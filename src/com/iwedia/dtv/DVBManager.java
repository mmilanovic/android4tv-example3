/*
 * Copyright (C) 2014 iWedia S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iwedia.dtv;

import android.content.Context;
import android.widget.Toast;

import com.iwedia.dtv.dtvmanager.DTVManager;
import com.iwedia.dtv.dtvmanager.IDTVManager;
import com.iwedia.dtv.route.broadcast.IBroadcastRouteControl;
import com.iwedia.dtv.route.broadcast.RouteDemuxDescriptor;
import com.iwedia.dtv.route.broadcast.RouteFrontendDescriptor;
import com.iwedia.dtv.route.broadcast.RouteFrontendType;
import com.iwedia.dtv.route.broadcast.RouteInstallSettings;
import com.iwedia.dtv.route.common.ICommonRouteControl;
import com.iwedia.dtv.route.common.RouteDecoderDescriptor;
import com.iwedia.dtv.route.common.RouteInputOutputDescriptor;
import com.iwedia.dtv.scan.IScanCallback;
import com.iwedia.dtv.scan.IScanControl;
import com.iwedia.dtv.scan.TunerType;
import com.iwedia.dtv.service.IServiceCallback;
import com.iwedia.dtv.service.Service;
import com.iwedia.dtv.service.ServiceDescriptor;
import com.iwedia.dtv.service.SourceType;
import com.iwedia.dtv.types.InternalException;

import java.util.EnumSet;

/**
 * DVBManager - Class For Handling MW Components.
 */
public class DVBManager {
    public static final String TAG = "DVBManager";
    /** DTV Service Intent Action. */
    private Context mContext = null;
    private IDTVManager mDTVManager = null;
    /** Live routes */
    private int mCurrentLiveRoute = 0;
    private int mLiveRouteSat = 0;
    private int mLiveRouteTer = 0;
    private int mLiveRouteCab = 0;
    private int mLiveRouteIp = 0;
    /** Install routes. */
    private static int mCurrentInstallRoute = 0;
    private int mInstallRouteIDTer = 0;
    private int mInstallRouteIDCab = 0;
    private int mInstallRouteIDSat = 0;
    private int mInstallRouteIDIp = 0;
    /** Currently active list in comedia. */
    private int mCurrentListIndex = 0;
    /** Listener for service connection change. */
    private static boolean scanStarted = false;
    private static boolean autoScan = false;

    public DVBManager(Context context) {
        mContext = context;
        mDTVManager = new DTVManager();
    }

    /**
     * Initialize Service.
     */
    public void InitializeDTVService() {
        initializeRouteId();
    }

    /**
     * Initialize Descriptors For Live Route.
     */
    private void initializeRouteId() {
        IBroadcastRouteControl broadcastRouteControl = mDTVManager
                .getBroadcastRouteControl();
        ICommonRouteControl commonRouteControl = mDTVManager
                .getCommonRouteControl();
        /**
         * RETRIEVE DEMUX DESCRIPTOR.
         */
        RouteDemuxDescriptor demuxDescriptor = broadcastRouteControl
                .getDemuxDescriptor(0);
        /**
         * RETRIEVE DECODER DESCRIPTOR.
         */
        RouteDecoderDescriptor decoderDescriptor = commonRouteControl
                .getDecoderDescriptor(0);
        /**
         * RETRIEVING OUTPUT DESCRIPTOR.
         */
        RouteInputOutputDescriptor outputDescriptor = commonRouteControl
                .getInputOutputDescriptor(0);
        /**
         * GET NUMBER OF FRONTENDS.
         */
        int numberOfFrontends = broadcastRouteControl.getFrontendNumber();
        /**
         * FIND DVB and IP front-end descriptors.
         */
        EnumSet<RouteFrontendType> frontendTypes = null;
        for (int i = 0; i < numberOfFrontends; i++) {
            RouteFrontendDescriptor frontendDescriptor = broadcastRouteControl
                    .getFrontendDescriptor(i);
            frontendTypes = frontendDescriptor.getFrontendType();
            for (RouteFrontendType frontendType : frontendTypes) {
                switch (frontendType) {
                    case SAT: {
                        if (mLiveRouteSat == 0) {
                            mLiveRouteSat = getLiveRouteId(frontendDescriptor,
                                    demuxDescriptor, decoderDescriptor,
                                    outputDescriptor, broadcastRouteControl);
                        }
                        if (mInstallRouteIDSat == 0) {
                            /**
                             * RETRIEVE INSTALL ROUTE ID SAT.
                             */
                            mInstallRouteIDSat = broadcastRouteControl
                                    .getInstallRoute(
                                            frontendDescriptor.getFrontendId(),
                                            demuxDescriptor.getDemuxId());
                        }
                        break;
                    }
                    case CAB: {
                        if (mLiveRouteCab == 0) {
                            mLiveRouteCab = getLiveRouteId(frontendDescriptor,
                                    demuxDescriptor, decoderDescriptor,
                                    outputDescriptor, broadcastRouteControl);
                        }
                        if (mInstallRouteIDCab == 0) {
                            /**
                             * RETRIEVE INSTALL ROUTE ID CAB.
                             */
                            mInstallRouteIDCab = broadcastRouteControl
                                    .getInstallRoute(
                                            frontendDescriptor.getFrontendId(),
                                            demuxDescriptor.getDemuxId());
                        }
                        break;
                    }
                    case TER: {
                        if (mLiveRouteTer == 0) {
                            mLiveRouteTer = getLiveRouteId(frontendDescriptor,
                                    demuxDescriptor, decoderDescriptor,
                                    outputDescriptor, broadcastRouteControl);
                        }
                        if (mInstallRouteIDTer == 0) {
                            /**
                             * RETRIEVE INSTALL ROUTE TER.
                             */
                            mInstallRouteIDTer = broadcastRouteControl
                                    .getInstallRoute(
                                            frontendDescriptor.getFrontendId(),
                                            demuxDescriptor.getDemuxId());
                        }
                        break;
                    }
                    case IP: {
                        if (mLiveRouteIp == 0) {
                            mLiveRouteIp = getLiveRouteId(frontendDescriptor,
                                    demuxDescriptor, decoderDescriptor,
                                    outputDescriptor, broadcastRouteControl);
                        }
                        if (mInstallRouteIDIp == 0) {
                            /**
                             * RETRIEVE INSTALL ROUTE IP.
                             */
                            mInstallRouteIDIp = broadcastRouteControl
                                    .getInstallRoute(
                                            frontendDescriptor.getFrontendId(),
                                            demuxDescriptor.getDemuxId());
                        }
                        break;
                    }
                    default:
                        break;
                }
            }
        }
    }

    /**
     * Get Live Route From Descriptors.
     * @param fDescriptor
     * @param mDemuxDescriptor
     * @param mDecoderDescriptor
     * @param mOutputDescriptor
     */
    private int getLiveRouteId(RouteFrontendDescriptor fDescriptor,
            RouteDemuxDescriptor mDemuxDescriptor,
            RouteDecoderDescriptor mDecoderDescriptor,
            RouteInputOutputDescriptor mOutputDescriptor,
            IBroadcastRouteControl routeControl) {
        return routeControl.getLiveRoute(fDescriptor.getFrontendId(),
                mDemuxDescriptor.getDemuxId(),
                mDecoderDescriptor.getDecoderId());
    }

    /**
     * Start auto scan procedure.
     * @param tunerType
     *        Type of tuner to scan channels.
     * @param keepCurrentList
     *        To keep channels in list or to clear channel list.
     * @return True if everything is ok, false otherwise.
     * @throws InternalException
     */
    public boolean autoScan(TunerType tunerType, boolean keepCurrentList)
            throws InternalException {
        int route = getActiveRouteByTunerType(tunerType);
        if (route == 0) {
            return false;
        }
        autoScan = true;
        IScanControl scanControl = mDTVManager.getScanControl();
        mCurrentInstallRoute = route;
        Service service = mDTVManager.getServiceControl().getActiveService(
                mCurrentLiveRoute);
        if (service.getServiceIndex() == -1) {
            scanControl.autoScan(route);
            return true;
        } else {
            scanStarted = true;
            stopDTV();
            return false;
        }
    }

    /**
     * Start manual scan procedure.
     * @param tunerType
     *        Type of tuner to scan channels.
     * @param frequency
     *        Entered frequency.
     * @param keepCurrentList
     *        To keep channels in list or to clear channel list.
     * @return True if everything is ok, false otherwise.
     * @throws InternalException
     */
    public boolean manualScan(TunerType tunerType, int frequency,
            boolean keepCurrentList) throws InternalException {
        int route = getActiveRouteByTunerType(tunerType);
        if (route == 0) {
            return false;
        }
        autoScan = false;
        IScanControl scanControl = mDTVManager.getScanControl();
        mCurrentInstallRoute = route;
        if (mCurrentLiveRoute == 0) {
            scanControl.setFrequency(frequency);
            scanControl.appendList(keepCurrentList);
            scanControl.manualScan(route);
            return true;
        } else {
            scanStarted = true;
            stopDTV();
            return false;
        }
    }

    /**
     * Start MW video playback.
     * @throws IllegalArgumentException
     * @throws InternalException
     */
    public void startDTV(int channelNumber) throws IllegalArgumentException,
            InternalException {
        if (channelNumber < 0 || channelNumber >= getChannelListSize()) {
            throw new IllegalArgumentException("Illegal channel index!");
        }
        ServiceDescriptor desiredService = mDTVManager.getServiceControl()
                .getServiceDescriptor(mCurrentListIndex, channelNumber);
        int route = getActiveRouteByServiceType(desiredService.getSourceType());
        if (route == 0) {
            Toast.makeText(mContext, "Undefined channel type!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mCurrentLiveRoute = route;
        mDTVManager.getServiceControl().startService(route, mCurrentListIndex,
                channelNumber);
    }

    /**
     * Stop MW video playback.
     * @throws InternalException
     */
    public void stopDTV() throws InternalException {
        ServiceDescriptor desiredService = mDTVManager.getServiceControl()
                .getServiceDescriptor(mCurrentListIndex,
                        getCurrentChannelNumber());
        int route = getActiveRouteByServiceType(desiredService.getSourceType());
        if (route == 0) {
            Toast.makeText(mContext, "Undefined channel type!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mDTVManager.getServiceControl().stopService(route);
    }

    /**
     * Get Current Channel Number.
     */
    public int getCurrentChannelNumber() {
        return mDTVManager.getServiceControl()
                .getActiveService(mCurrentLiveRoute).getServiceIndex();
    }

    /**
     * Get Size of Channel List.
     */
    public int getChannelListSize() {
        return mDTVManager.getServiceControl().getServiceListCount(
                mCurrentListIndex);
    }

    /**
     * Return route by tuner type.
     * @param tunerType
     *        Tuner type to check.
     * @return Desired route, or 0 if service type is undefined.
     */
    private int getActiveRouteByTunerType(TunerType tunerType) {
        IBroadcastRouteControl lBroadcastRouteControl = mDTVManager
                .getBroadcastRouteControl();
        RouteInstallSettings lRouteInstallSettings = new RouteInstallSettings();
        int lInstallRoute = 0;
        switch (tunerType) {
            case SATTELITE: {
                lRouteInstallSettings.setFrontendType(RouteFrontendType.SAT);
                lInstallRoute = mInstallRouteIDSat;
                break;
            }
            case CABLE: {
                lRouteInstallSettings.setFrontendType(RouteFrontendType.CAB);
                lInstallRoute = mInstallRouteIDCab;
                break;
            }
            case TERRESTRIAL: {
                lRouteInstallSettings.setFrontendType(RouteFrontendType.TER);
                lInstallRoute = mInstallRouteIDTer;
                break;
            }
            case IP: {
                lRouteInstallSettings.setFrontendType(RouteFrontendType.IP);
                lInstallRoute = mInstallRouteIDIp;
                break;
            }
            default:
                return 0;
        }
        lBroadcastRouteControl.configureInstallRoute(lInstallRoute,
                lRouteInstallSettings);
        return lInstallRoute;
    }

    /**
     * Return route by service type.
     * @param serviceType
     *        Service type to check.
     * @return Desired route, or 0 if service type is undefined.
     */
    private int getActiveRouteByServiceType(SourceType sourceType) {
        switch (sourceType) {
            case CAB: {
                return mLiveRouteCab;
            }
            case TER: {
                return mLiveRouteTer;
            }
            case SAT: {
                return mLiveRouteSat;
            }
            case IP: {
                return mLiveRouteIp;
            }
            default:
                return 0;
        }
    }

    /**
     * Abort started scan.
     * @return True if everything is ok, false otherwise.
     * @throws InternalException
     */
    public void abortScan() throws InternalException {
        mDTVManager.getScanControl().abortScan(mCurrentInstallRoute);
    }

    /**
     * Sets scan callback.
     * @param channelCallback
     *        Scan callback object.
     */
    public void setChannelCallback(IServiceCallback channelCallback) {
        mDTVManager.getServiceControl().registerCallback(channelCallback);
    }

    /**
     * Remove callback.
     */
    public void removeChannelCallback() {
        mDTVManager.getServiceControl().unregisterCallback(null);
    }

    /**
     * Sets scan callback.
     * @param scanCallback
     *        Scan callback object.
     */
    public void setScanCallback(IScanCallback scanCallback) {
        mDTVManager.getScanControl().registerCallback(scanCallback);
    }

    /**
     * Remove callback.
     */
    public void removeScanCallback() {
        mDTVManager.getScanControl().unregisterCallback(null);
    }

    public long getCurrentInstallRoute() {
        return mCurrentInstallRoute;
    }

    public IDTVManager getmDTVManager() {
        return mDTVManager;
    }

    public static boolean isScanStarted() {
        return scanStarted;
    }

    public static void setScanStarted(boolean scanStarted) {
        DVBManager.scanStarted = scanStarted;
    }

    public static boolean isAutoScan() {
        return autoScan;
    }
}
