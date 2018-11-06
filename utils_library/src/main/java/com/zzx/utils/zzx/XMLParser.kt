package com.zzx.utils.zzx


import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler

import java.io.File

import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory

/**@author Tomy
 * Created by Tomy on 2014/9/26.
 */
class XMLParser private constructor() {
    private var mInfo: ServiceInfo? = null

    private val mHandler = XMLContentHandler()

    fun parserXMLFile(filePath: String): ServiceInfo? {
        val file = File(filePath)
        if (!file.exists())
            return null
        try {
            mSaxParser!!.parse(File(filePath), mHandler)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        return mInfo
    }

    fun release() {
        mSaxParser!!.reset()
        mSaxParser = null
        mXmlParser = null
    }

    private inner class XMLContentHandler : DefaultHandler() {
        private var mFlag: String? = null
        @Throws(SAXException::class)
        override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
            mFlag = localName
        }

        @Throws(SAXException::class)
        override fun endElement(uri: String, localName: String, qName: String) {
            mFlag = ""
        }

        @Throws(SAXException::class)
        override fun startDocument() {
            if (mInfo == null) {
                mInfo = ServiceInfo()
            }
        }

        @Throws(SAXException::class)
        override fun endDocument() {
        }

        @Throws(SAXException::class)
        override fun characters(ch: CharArray, start: Int, length: Int) {
            val value = String(ch, start, length)
            when (mFlag) {
                XML_SERVICE_IP -> mInfo!!.mServiceIp = value
                XML_SERVICE_PORT -> mInfo!!.mPort = Integer.parseInt(value)
            }
        }
    }

    companion object {
        const val XML_SERVICE_PORT = "port"
        const val XML_SERVICE_IP = "ip"
        private var mSaxParser: SAXParser? = null
        private var mXmlParser: XMLParser? = null

        val instance: XMLParser
            get() {
                if (mSaxParser == null) {
                    try {
                        mSaxParser = SAXParserFactory.newInstance().newSAXParser()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
                if (mXmlParser == null) {
                    mXmlParser = XMLParser()
                }
                return mXmlParser!!
            }
    }

}
