package com.emptech.biocollectiononline.common;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

public class PullParser {

	public static interface ICallback {
		public void callback(int eventCode, XmlPullParser parser) throws XmlPullParserException, IOException;
	}

	private PullParser() {
	}

	/**
	 * @param cb
	 * @param charset
	 * @param inputstream xml文件输入流
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public static void readXML(ICallback cb, String charset, InputStream inputstream) throws XmlPullParserException,
            IOException {
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(inputstream, charset);
		int eventCode = parser.getEventType();//获取xml解析器当前节点
		while (eventCode != XmlPullParser.END_DOCUMENT) {
			if (cb != null)
				cb.callback(eventCode, parser);//将解析器和当前节点通过回调接口传出去
			eventCode = parser.next();//下一个节点
		}
	}
}
