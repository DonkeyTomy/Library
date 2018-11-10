package com.zzx.utils.zzx

import android.content.Context
import android.provider.Settings

/**@author Tomy
 * Created by Tomy on 2014/10/23.
 */
object DeviceUtils {
    private const val MODEL_NEED_PRE = "ZZXNeedPre"
    const val CFG_ENABLED_PRISON = "ZZXPrisonEnabled"
    fun isPrisonEnabled(context: Context): Boolean {
        return Settings.System.getInt(context.contentResolver, CFG_ENABLED_PRISON, -1) == 1
    }

    fun getUserNum(context: Context): String {
        var num: String? = Settings.System.getString(context.contentResolver,
                POLICE_NUMBER)
        if (num == null || num == "")
            num = if (isPrisonEnabled(context)) POLICE_DEFAULT_NUM_8 else POLICE_DEFAULT_NUM
        return num
    }

    fun getDeviceNum(context: Context): String {
        var num: String? = Settings.System.getString(context.contentResolver, DEVICE_NUMBER)
        if (num == null || num == "")
            num = DEVICE_DEFAULT_ONLY_NUM
        return num
    }

    fun getModelName(context: Context): String {
        val model = Settings.System.getString(context.contentResolver, MODEL_NAME)
        return if (model == null || model == "") {
            MODEL_DEFAULT_NAME
        } else model
    }

    fun getPrisonerNum(context: Context): String? {
        var num: String? = Settings.System.getString(context.contentResolver,
                PRISONER_NUMBER)
        if (num == null || num == "")
            num = PRISONER_DEFAULT_NUM
        return num
    }

    fun writePrisonerNum(context: Context, prisonerNum: String?): Boolean {
        return !(prisonerNum == null || !prisonerNum.matches("[A-Za-z0-9]{10}".toRegex())) && Settings.System.putString(context.contentResolver,
                PRISONER_NUMBER, prisonerNum)
    }

    fun writePoliceNum(context: Context, policeNum: String?): Boolean {
        if (policeNum == null) {
            return false
        }
        if (isPrisonEnabled(context)) {
            if (!policeNum.matches("[A-Za-z0-9]{8}".toRegex())) {
                return false
            }
        } else {
            if (!policeNum.matches("[A-Za-z0-9]{6}".toRegex())) {
                return false
            }
        }

        return Settings.System.putString(context.contentResolver,
                POLICE_NUMBER, policeNum)
    }

    fun writeDeviceNum(context: Context, deviceNum: String?): Boolean {
        return !(deviceNum == null || !deviceNum.matches("[A-Za-z0-9]{5}".toRegex())) && Settings.System.putString(context.contentResolver, DEVICE_NUMBER, deviceNum)
    }

    fun getServiceInfo(mContext: Context): ServiceInfo? {
        var info: ServiceInfo? = ServiceInfo()
        val resolver = mContext.contentResolver
        val serviceIP = Settings.System.getString(resolver, SERVICE_IP_ADDRESS)
        val portStr = Settings.System.getString(resolver, SERVICE_IP_PORT)
        if (serviceIP != null && portStr != null && serviceIP != "" && portStr != "") {
            try {
                val port = Integer.parseInt(portStr)
                info!!.mServiceIp = serviceIP
                info.mPort = port
            } catch (e: Exception) {
                return null
            }

        } else {
            val parser = XMLParser.instance
            info = parser.parserXMLFile("/etc/service.xml")
            parser.release()
        }
        return info
    }

    fun checkHasService(context: Context): Boolean {
        val info = DeviceUtils.getServiceInfo(context)
        return info != null
    }


    const val PATH_POLICE_NUMBER = "/sys/devices/platform/zzx-misc/police_num_stats"
    const val PATH_GPS_INFO = "/sys/devices/platform/zzx-misc/gps_stats"
    const val POLICE_DEFAULT_NUM = "654321"
    const val PRISONER_NUMBER = "PrisonerNumber"
    const val POLICE_DEFAULT_NUM_8 = "00000000"
    const val PRISONER_DEFAULT_NUM = "0000000000"
    const val DEVICE_DEFAULT_NUM = "DSJ-00-00000"
    const val DEVICE_DEFAULT_ONLY_NUM = "12345"
    const val SERVICE_IP_ADDRESS = "service_ip_address"
    const val SERVICE_IP_PORT = "service_ip_port"
    const val POLICE_NUMBER = "police_number"
    const val DEVICE_NUMBER = "device_number"
    const val DEVICE_PRE = "DSJ-"
    const val MODEL_NAME = "model_name"
    const val MODEL_DEFAULT_NAME = "00"
}
