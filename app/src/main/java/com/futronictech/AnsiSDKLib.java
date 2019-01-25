/**
 * Last edit by YZQ, www.xiongdi.cn, 2016-8-4
 * Please Make sure the package path must be "package com.futronictech;"
 */
package com.futronictech;

import android.annotation.SuppressLint;

public class AnsiSDKLib {
    /**
     *  opens device on the selected interface.
     *  @return true if the function succeeds, false if the function fails. Use {@link #GetErrorMessage} to know more.
     */
    public native boolean OpenDevice(int instance);

    /**
     * @see #OpenDevice(int instance)
     * @param io_ctx A instance object called by the JNI.
     * @return true if the function succeeds, false if the function fails. Use {@link #GetErrorMessage} to know more.
     */
	public native boolean OpenDeviceCtx(Object io_ctx);

    /**
     * Closes an device opened by {@link #OpenDevice} or {@link #OpenDeviceCtx}
     * @return true if the function succeeds, false if the function fails. Use {@link #GetErrorMessage} to know more.
     */
    public native boolean CloseDevice();

    /**
     * Fill value to {@link #m_ImageWidth} and {@link #m_ImageHeight}
     * @return true if the function succeeds, false if the function fails. Use {@link #GetErrorMessage} to know more.
     */
    public native boolean FillImageSize();

    /**
     * @return a Boolean value that indicates whether the fingerprint is present on the device. Use {@link #GetErrorMessage} to know more.
     */
    public native boolean IsFingerPresent();

    /**
     * Captures a frame from the device.
     * @param pImage [out] A pointer to the buffer that receives the frame.
     * @return true if the function succeeds, false if the function fails. Use {@link #GetErrorMessage} to know more.
     */
    public native boolean CaptureImage(byte[] pImage);

    /**
     * Captures a frame from the device and creates the template from the captured frame.
     * @param finger [in] The finger position code. This parameter can be one of the following values[0x00, 0x0A],
     *               see{@link #FTR_ANSISDK_FINGPOS_UK}.
     * @param pImage [out] A pointer to the buffer that receives the captured frame.
     * @param pTemplate [out] A pointer to the buffer that receives the processed template.
     * @param pTemplateSize [out, optional] A pointer to the variable that receives the number of
     *                      bytes in the processed template. When the function returns, the variable
     *                      contains the number of bytes written to the buffer.
     * @return true if the function succeeds, false if the function fails. Use {@link #GetErrorMessage} to know more.
     */
	public native boolean CreateTemplate(int finger, byte[] pImage, byte[] pTemplate, int[] pTemplateSize);

    /**
     * Captures a frame from the device and compares it with submitted template and outputs a match score.
     * @param finger [in] The finger position code. This parameter can be one of the following values[0x00, 0x0A].
     *               see{@link #FTR_ANSISDK_FINGPOS_UK}
     * @param pImage [out] A pointer to the buffer that receives the captured frame.
     * @param pTemplate [in] A pointer to the template compliant with ANSI INCITS 378-2004 or ISO/IEC 19794-2:2005
     *                  format. This template returned by the {@link #CreateTemplate} function.
     * @param pVerifyResult [out, optional] A pointer to the variable that receives a similarity score resulting
     *                      from comparison of the captured and submitted templates.
     * @return true if the function succeeds, false if the function fails. Use {@link #GetErrorMessage} to know more.
     */
    public native boolean VerifyTemplate(int finger, byte[] pImage, byte[] pTemplate, float[] pVerifyResult);

    /**
     * Compares two compliant templates and outputs a match score.
     * @param pProbeTemplate [in] A pointer to the template compliant with ANSI INCITS 378-2004.
     *                       This template returned by the {@link #CreateTemplate} function.
     * @param pGaleryTemplate [in] A pointer to the template compliant with ANSI INCITS 378-2004 or ISO/IEC 19794-2:2005.
     *                        This template returned by the {@link #CreateTemplate} function.
     * @param pMatchResult [out, optional] A pointer to the variable that receives a similarity score
     *                     resulting from comparison of the templates.
     * @return true if the function succeeds, false if the function fails. Use {@link #GetErrorMessage} to know more.
     */
	public native boolean MatchTemplates(byte[] pProbeTemplate, byte[] pGaleryTemplate, float[] pMatchResult);

    /**
     * Converts ANSI INCITS 378-2004 template to ISO/IEC 19794-2:2005 format template.
     * @param pAnsiTemplate [in] A pointer to the ANSI INCITS 378-2004 format template.
     * @param pIsoTemplate [out] A pointer to the buffer that receives the ISO/IEC 19794-2:2005 format template.
     * @param pIsoTemplateSize [in, out, optional] A pointer to a variable that specifies the size of the buffer
     *                         pointed to by the {@param pAnsiTemplate} parameter, in bytes. When the function
     *                         returns, this variable contains the size of the data copied to {@param pIsoTemplate}.
     * @return true if the function succeeds, false if the function fails. Use {@link #GetErrorMessage} to know more.
     */
	public native boolean ConvertAnsiTemplateToIso(byte[] pAnsiTemplate, byte[] pIsoTemplate, int[] pIsoTemplateSize);

    /**
     * Retrieves the maximum template size.
     * @return the maximum template size.
     */
	public native int GetMaxTemplateSize();
	
	public native boolean GetNfiqFromImage(byte[] pImage, int nImageWidth, int nImageHeight);    
	public native boolean CreateTemplateFromImage(int finger, byte[] pImage, int nImageWidth, int nImageHeight, byte[] pTemplate, int[] pTemplateSize);
	
    // error code
	static public final int FTR_ANSISDK_ERROR_NO_ERROR   =           0;
	static public final int FTR_ERROR_EMPTY_FRAME = 4306; /* ERROR_EMPTY */
	static public final int FTR_ERROR_MOVABLE_FINGER = 0x20000001;
	static public final int FTR_ERROR_NO_FRAME = 0x20000002;
	static public final int FTR_ERROR_HARDWARE_INCOMPATIBLE = 0x20000004;
	static public final int FTR_ERROR_FIRMWARE_INCOMPATIBLE = 0x20000005;
	static public final int FTR_ERROR_INVALID_AUTHORIZATION_CODE = 0x20000006;
	static public final int FTR_ERROR_WRITE_PROTECT = 19;
	static public final int FTR_ERROR_NOT_READY = 21;
	static public final int FTR_ERROR_NOT_ENOUGH_MEMORY = 8;
	static public final int FTR_ERROR_NO_MORE_ITEMS = 259; /*No item during device enum*/
	
	static public final int FTR_ERROR_NO_ERROR   =           0;
	static public final int FTR_ANSISDK_ERROR_IMAGE_SIZE_NOT_SUP = 0x30000001;
	static public final int FTR_ANSISDK_ERROR_EXTRACTION_UNSPEC = 0x30000002;
	static public final int FTR_ANSISDK_ERROR_EXTRACTION_BAD_IMP = 0x30000003;
	static public final int FTR_ANSISDK_ERROR_MATCH_NULL = 0x30000004; 
	static public final int FTR_ANSISDK_ERROR_MATCH_PARSE_PROBE = 0x30000005; 
	static public final int FTR_ANSISDK_ERROR_MATCH_PARSE_GALLERY = 0x30000006; 
	static public final int FTR_ANSISDK_ERROR_MORE_DATA = 0x30000007; 
	
	/* Position type codes */
	static public final int FTR_ANSISDK_FINGPOS_UK =                 0x00;    /* Unknown finger */
	static public final int FTR_ANSISDK_FINGPOS_RT =                 0x01;    /* Right thumb */
	static public final int FTR_ANSISDK_FINGPOS_RI =                 0x02;    /* Right index finger */
	static public final int FTR_ANSISDK_FINGPOS_RM =                 0x03;    /* Right middle finger */
	static public final int FTR_ANSISDK_FINGPOS_RR =                 0x04;    /* Right ring finger */
	static public final int FTR_ANSISDK_FINGPOS_RL =                 0x05;    /* Right little finger */
	static public final int FTR_ANSISDK_FINGPOS_LT =                 0x06;    /* Left thumb */
	static public final int FTR_ANSISDK_FINGPOS_LI =                 0x07;    /* Left index finger */
	static public final int FTR_ANSISDK_FINGPOS_LM =                 0x08;    /* Left middle finger */
	static public final int FTR_ANSISDK_FINGPOS_LR =                 0x09;    /* Left ring finger */
	static public final int FTR_ANSISDK_FINGPOS_LL =                 0x0A;    /* Left little finger */
	
	static public final float FTR_ANSISDK_MATCH_SCORE_LOW =            37;      /* FAR = 1% */
	static public final float FTR_ANSISDK_MATCH_SCORE_LOW_MEDIUM =     65;     /* FAR = 0.1% */
	static public final float FTR_ANSISDK_MATCH_SCORE_MEDIUM =         93;     /* FAR = 0.01% */
	static public final float FTR_ANSISDK_MATCH_SCORE_HIGH_MEDIUM =    121;     /* FAR = 0.001% */
	static public final float FTR_ANSISDK_MATCH_SCORE_HIGH =           146;     /* FAR = 0.0001% */
	static public final float FTR_ANSISDK_MATCH_SCORE_VERY_HIGH =      189;     /* FAR = 0 */
    
    private final int kDefaultDeviceInstance = 0;

    public AnsiSDKLib()
    {
    	m_hDevice = 0;
    	m_ImageWidth = m_ImageHeight = 0;
    	m_ErrorCode = 0;
    	m_NFIQ = 0;
    }
    
    public int GetImageWidth()
    {
        return m_ImageWidth;
    }
    public int GetImageHeight()
    {
        return m_ImageHeight;
    }

    /**
     * retrieves the image size.
     * @return the image size.
     */
	public int GetImageSize()
	{
		return m_ImageWidth * m_ImageHeight;
	}

    /**
     * {@link #m_ErrorCode} was set by SetErrorCode[JNI].
     * @return ErrorCode.
     */
    public int GetErrorCode()
    {
    	return m_ErrorCode;
    }

    //1-5  1最好
	public int GetNIFQValue()
	{
		return m_NFIQ;
	}    

    @SuppressLint("DefaultLocale") public String GetErrorMessage()
    {
        String strErrMsg;
        switch(m_ErrorCode)
        {
            case FTR_ERROR_NO_ERROR:
                strErrMsg = "OK";
                break;
            case FTR_ERROR_EMPTY_FRAME:
                strErrMsg = "Empty Frame";
                break;
            case FTR_ERROR_MOVABLE_FINGER:
                strErrMsg = "Moveable Finger";
                break;
            case FTR_ERROR_NO_FRAME:
                strErrMsg = "Fake Finger";
                break;
            case FTR_ERROR_HARDWARE_INCOMPATIBLE:
                strErrMsg = "Hardware Incompatible";
                break;
            case FTR_ERROR_FIRMWARE_INCOMPATIBLE:
                strErrMsg = "Firmware Incompatible";
                break;
            case FTR_ERROR_INVALID_AUTHORIZATION_CODE:
                strErrMsg = "Invalid Authorization Code";
                break;
            case FTR_ERROR_WRITE_PROTECT:
                strErrMsg = "Write Protect";
                break;
            case FTR_ERROR_NO_MORE_ITEMS:
            	strErrMsg = "Device not connected";
            	break;
            default:
                strErrMsg = String.format("Error code is %d", m_ErrorCode);
                break;
        }
        return strErrMsg;
    }
            
            
    static 
    {
    	System.loadLibrary("usb-1.0");
    }
    
    static 
    {
    	System.loadLibrary("ftrScanAPI");
    }
	
	static 
	{
		System.loadLibrary("ftrMathAPIAndroid");
    }

	static 
	{
		System.loadLibrary("ftrAnsiSDK");
    }
	
    static 
    {
    	System.loadLibrary("ftrAnsiSDKAndroidJni");
    }
    
    private int m_ImageWidth;
    private int m_ImageHeight;
    private long m_hDevice;
    private int m_ErrorCode;
    private int m_NFIQ;   
   
}
