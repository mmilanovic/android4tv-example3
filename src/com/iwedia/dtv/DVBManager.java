/*
 * Copyright (C) 2014 iWedia S.A. Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.iwedia.dtv;

import android.os.RemoteException;
import android.util.Log;

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
import com.iwedia.dtv.scan.Modulation;
import com.iwedia.dtv.scan.SignalInfo;
import com.iwedia.dtv.scan.TunerType;
import com.iwedia.dtv.service.IServiceCallback;
import com.iwedia.dtv.service.IServiceControl;
import com.iwedia.dtv.service.Service;
import com.iwedia.dtv.service.ServiceDescriptor;
import com.iwedia.dtv.service.SourceType;
import com.iwedia.dtv.types.InternalException;

import java.util.ArrayList;
import java.util.EnumSet;

/**
 * DVBManager - Class For Handling MW Components.
 */
public class DVBManager {
    public static final String TAG = "DVBManager";
    /** DTV Service Intent Action. */
    private IDTVManager mDTVManager = null;
    /** Live routes */
    private int mCurrentLiveRoute = -1;
    private int mLiveRouteSat = -1;
    private int mLiveRouteTer = -1;
    private int mLiveRouteCab = -1;
    private int mLiveRouteIp = -1;
    /** Install routes. */
    private static int mCurrentInstallRoute = -1;
    private int mInstallRouteIDTer = -1;
    private int mInstallRouteIDCab = -1;
    private int mInstallRouteIDSat = -1;
    private int mInstallRouteIDIp = -1;
    /** Currently active list in comedia. */
    private final int CURRENT_LIST_INDEX = 0;
    /** Listener for service connection change. */
    private static boolean scanStarted = false;
    private static boolean autoScan = false;
    private static boolean networkManualScan = false;
    private static DVBManager mInstance;
    private boolean ipAndSomeOtherTunerType = false;

    public static DVBManager getInstance() {
        if (mInstance == null) {
            mInstance = new DVBManager();
        }
        return mInstance;
    }

    private DVBManager() {
        mDTVManager = new DTVManager();
        initializeDTVService();
    }

    /**
     * Initialize Service.
     */
    private void initializeDTVService() {
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
                        if (mLiveRouteSat == -1) {
                            mLiveRouteSat = getLiveRouteId(frontendDescriptor,
                                    demuxDescriptor, decoderDescriptor,
                                    outputDescriptor, broadcastRouteControl);
                        }
                        if (mInstallRouteIDSat == -1) {
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
                        if (mLiveRouteCab == -1) {
                            mLiveRouteCab = getLiveRouteId(frontendDescriptor,
                                    demuxDescriptor, decoderDescriptor,
                                    outputDescriptor, broadcastRouteControl);
                        }
                        if (mInstallRouteIDCab == -1) {
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
                        if (mLiveRouteTer == -1) {
                            mLiveRouteTer = getLiveRouteId(frontendDescriptor,
                                    demuxDescriptor, decoderDescriptor,
                                    outputDescriptor, broadcastRouteControl);
                        }
                        if (mInstallRouteIDTer == -1) {
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
                        if (mLiveRouteIp == -1) {
                            mLiveRouteIp = getLiveRouteId(frontendDescriptor,
                                    demuxDescriptor, decoderDescriptor,
                                    outputDescriptor, broadcastRouteControl);
                        }
                        if (mInstallRouteIDIp == -1) {
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
        if (mLiveRouteIp != -1
                && (mLiveRouteCab != -1 || mLiveRouteSat != -1 || mLiveRouteTer != -1)) {
            ipAndSomeOtherTunerType = true;
        }
    }

    /**
     * Get Live Route From Descriptors.
     * 
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
     * 
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
        if (route == -1) {
            return false;
        }
        autoScan = true;
        IScanControl scanControl = mDTVManager.getScanControl();
        mCurrentInstallRoute = route;
        Service service = mDTVManager.getServiceControl().getActiveService(
                mCurrentLiveRoute);
        if (mCurrentLiveRoute == -1 || service.getServiceIndex() == -1
                || scanStarted) {
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
     * 
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
        if (route == -1) {
            return false;
        }
        autoScan = false;
        IScanControl scanControl = mDTVManager.getScanControl();
        mCurrentInstallRoute = route;
        Service service = mDTVManager.getServiceControl().getActiveService(
                mCurrentLiveRoute);
        if (mCurrentLiveRoute == -1 || service.getServiceIndex() == -1
                || scanStarted) {
            scanControl.setFrequency(frequency);
            scanControl.appendList(keepCurrentList);
            if (networkManualScan) {
                scanControl.manualNitScan(route);
            } else {
                scanControl.manualScan(route);
            }
            return true;
        } else {
            scanStarted = true;
            stopDTV();
            return false;
        }
    }

    /**
     * Sets modulation.
     * 
     * @param modulation
     *        Index of modulation option.
     * @throws RemoteException
     *         If connection error happens.
     */
    public void setModulation(int modulation) {
        mDTVManager.getScanControl().setModulation(
                Modulation.values()[modulation]);
    }

    /**
     * Returns active modulation.
     * 
     * @throws RemoteException
     */
    public int getModulation() {
        return mDTVManager.getScanControl().getModulation().ordinal();
    }

    /**
     * Sets symbol rate.
     * 
     * @param symbolRate
     *        Symbol rate to set.
     * @throws RemoteException
     *         If connection error happens.
     */
    public void setSymbolRate(int symbolRate) {
        mDTVManager.getScanControl().setSymbolRate(symbolRate);
    }

    /**
     * Returns active symbol rate.
     * 
     * @throws RemoteException
     */
    public int getSymbolRate() {
        return (int) mDTVManager.getScanControl().getSymbolRate();
    }

    /**
     * Sets network number.
     * 
     * @param networkNumber
     *        Network number to set.
     * @throws RemoteException
     *         If connection error happens.
     */
    public void setNetworkNumber(int networkNumber) {
        if (networkNumber > 0) {
            networkManualScan = true;
        } else {
            networkManualScan = false;
        }
        mDTVManager.getScanControl().setNetworkNumber(networkNumber);
    }

    /**
     * Returns active network number.
     * 
     * @throws RemoteException
     */
    public int getNetworkNumber() {
        return mDTVManager.getScanControl().getNetworkNumber();
    }

    /**
     * Start MW video playback.
     * 
     * @throws InternalException
     * @throws IllegalArgumentException
     */
    public void startDTV(int channelNumber) throws IllegalArgumentException,
            InternalException {
        if (channelNumber < 0 || channelNumber >= getChannelListSize()) {
            throw new IllegalArgumentException("Illegal channel index!");
        }
        ServiceDescriptor desiredService = mDTVManager.getServiceControl()
                .getServiceDescriptor(CURRENT_LIST_INDEX, channelNumber);
        int route = getActiveRouteByServiceType(desiredService.getSourceType());
        /** Wrong route */
        if (route == -1 && mLiveRouteIp == -1) {
            return;
        } else {
            /** There is IP and DVB */
            if (ipAndSomeOtherTunerType) {
                desiredService = mDTVManager.getServiceControl()
                        .getServiceDescriptor(CURRENT_LIST_INDEX,
                                channelNumber + 1);
                Log.d(TAG, "desiredService name " + desiredService.getName());
                route = getActiveRouteByServiceType(desiredService
                        .getSourceType());
                int numberOfDtvChannels = getChannelListSize();
                /** Regular DVB channel */
                if (channelNumber < numberOfDtvChannels) {
                    mCurrentLiveRoute = route;
                    mDTVManager.getServiceControl().startService(route,
                            CURRENT_LIST_INDEX, channelNumber + 1);
                }
            }
            /** Only DVB */
            else {
                mCurrentLiveRoute = route;
                mDTVManager.getServiceControl().startService(route,
                        CURRENT_LIST_INDEX, channelNumber);
            }
        }
    }

    /**
     * Stop MW video playback.
     * 
     * @throws InternalException
     */
    public void stopDTV() throws InternalException {
        mDTVManager.getServiceControl().stopService(mCurrentLiveRoute);
    }

    /**
     * Change Channel by Number.
     * 
     * @return Channel Info Object or null if error occurred.
     * @throws IllegalArgumentException
     * @throws InternalException
     */
    public void changeChannelByNumber(int channelNumber)
            throws InternalException {
        Log.d(TAG, "setChannel, channelNumber: " + channelNumber);
        channelNumber = (channelNumber + getChannelListSize())
                % getChannelListSize();
        int numberOfDtvChannels = getChannelListSize();
        /** For regular DVB channel */
        if (channelNumber < numberOfDtvChannels) {
            ServiceDescriptor desiredService = mDTVManager.getServiceControl()
                    .getServiceDescriptor(
                            CURRENT_LIST_INDEX,
                            ipAndSomeOtherTunerType ? channelNumber + 1
                                    : channelNumber);
            // if (desiredService.isScrambled()) {
            // return null;
            // }
            int route = getActiveRouteByServiceType(desiredService
                    .getSourceType());
            if (route == -1) {
                return;
            }
            mCurrentLiveRoute = route;
            mDTVManager.getServiceControl()
                    .startService(
                            route,
                            CURRENT_LIST_INDEX,
                            ipAndSomeOtherTunerType ? channelNumber + 1
                                    : channelNumber);
        }
    }

    /**
     * Get Current Channel Number.
     */
    public int getCurrentChannelNumber() {
        return (int) (mDTVManager.getServiceControl().getActiveService(
                mCurrentLiveRoute).getServiceIndex())
                - (ipAndSomeOtherTunerType ? 1 : 0);
    }

    /**
     * Get Size of Channel List.
     */
    public int getChannelListSize() {
        int serviceCount = mDTVManager.getServiceControl().getServiceListCount(
                CURRENT_LIST_INDEX);
        if (ipAndSomeOtherTunerType) {
            serviceCount--;
        }
        return serviceCount;
    }

    /**
     * Get Channel Names.
     */
    public ArrayList<String> getChannelNames() {
        ArrayList<String> channelNames = new ArrayList<String>();
        String channelName = "";
        int channelListSize = getChannelListSize();
        IServiceControl serviceControl = mDTVManager.getServiceControl();
        /** If there is IP first element in service list is DUMMY */
        channelListSize = ipAndSomeOtherTunerType ? channelListSize + 1
                : channelListSize;
        for (int i = ipAndSomeOtherTunerType ? 1 : 0; i < channelListSize; i++) {
            channelName = serviceControl.getServiceDescriptor(
                    CURRENT_LIST_INDEX, i).getName();
            channelNames.add(channelName);
        }
        return channelNames;
    }

    /**
     * Return route by tuner type.
     * 
     * @param tunerType
     *        Tuner type to check.
     * @return Desired route, or 0 if service type is undefined.
     */
    private int getActiveRouteByTunerType(TunerType tunerType) {
        IBroadcastRouteControl lBroadcastRouteControl = mDTVManager
                .getBroadcastRouteControl();
        RouteInstallSettings lRouteInstallSettings = new RouteInstallSettings();
        int lInstallRoute = -1;
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
                return -1;
        }
        lBroadcastRouteControl.configureInstallRoute(lInstallRoute,
                lRouteInstallSettings);
        return lInstallRoute;
    }

    /**
     * Return route by service type.
     * 
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
                return -1;
        }
    }

    /**
     * Abort started scan.
     * 
     * @throws InternalException
     */
    public void abortScan() throws InternalException {
        mDTVManager.getScanControl().abortScan(mCurrentInstallRoute);
    }

    /**
     * Sets scan callback.
     * 
     * @param channelCallback
     *        Scan callback object.
     */
    public void setChannelCallback(IServiceCallback channelCallback) {
        mDTVManager.getServiceControl().registerCallback(channelCallback);
    }

    /**
     * Remove callback.
     */
    public void removeChannelCallback(IServiceCallback channelCallback) {
        mDTVManager.getServiceControl().unregisterCallback(channelCallback);
    }

    /**
     * Sets scan callback.
     * 
     * @param scanCallback
     *        Scan callback object.
     */
    public void setScanCallback(IScanCallback scanCallback) {
        mDTVManager.getScanControl().registerCallback(scanCallback);
    }

    /**
     * Remove callback.
     */
    public void removeScanCallback(IScanCallback scanCallback) {
        mDTVManager.getScanControl().unregisterCallback(scanCallback);
    }

    public SignalInformation getServiceInfo() {
        Service activeService = mDTVManager.getServiceControl()
                .getActiveService(mCurrentLiveRoute);
        ServiceDescriptor descriptor = mDTVManager.getServiceControl()
                .getServiceDescriptor(CURRENT_LIST_INDEX,
                        activeService.getServiceIndex());
        SignalInfo signalInfo = mDTVManager.getScanControl().getSignalInfo(
                mCurrentLiveRoute);
        return new SignalInformation(descriptor.getName(),
                descriptor.getONID(), signalInfo.getSignalQuality(),
                signalInfo.getSignalStrenght(), descriptor.getFrequency());
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
