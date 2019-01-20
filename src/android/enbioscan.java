package cordova.plugin.enbioscan;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class enbioscan extends CordovaPlugin {
    private byte[]					byTemplate1;
    private byte[]					byTemplate2;

    private byte[]					byCapturedRaw1;
    private int						nCapturedRawWidth1;
    private int						nCapturedRawHeight1;

    private byte[]					byCapturedRaw2;
    private int						nCapturedRawWidth2;
    private int						nCapturedRawHeight2;

    public static final int QUALITY_LIMIT = 60;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        }
        if (action.equals("init")) {
            this.init(callbackContext);
            return true;
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void init(CallbackContext callbackContext) {
        NBioBSPJNI.CURRENT_PRODUCT_ID = 0;
        if (bsp == null) {
            bsp = new NBioBSPJNI ( "010701-613E5C7F4CC7C4B0-72E340B47E034015", this, mCallback );
            String msg = null;
            if (bsp.IsErrorOccured ( )){
                 msg = "NBioBSP Error: " + bsp.GetErrorCode ( );
                callbackContext.error(msg);
            }
            else {
                msg = "SDK Version: " + bsp.GetVersion ( );
                msg = " Intialize successfull";
                exportEngine = bsp.new Export ( );
                indexSearch = bsp.new IndexSearch ( );
                callbackContext.success(message);
            }
        }
    }


    NBioBSPJNI.CAPTURE_CALLBACK mCallback = new NBioBSPJNI.CAPTURE_CALLBACK ( ) {

        public void OnDisConnected() {
            Log.d ( "error", String.valueOf ( bsp.GetErrorCode ( ) ) );
            NBioBSPJNI.CURRENT_PRODUCT_ID = 0;


            String message = "NBioBSP Disconnected: " + bsp.GetErrorCode ( );
            txt.setText ( message );


        }

        public void OnConnected() {

            String message = "Device Open Success : ";

            ByteBuffer deviceId = ByteBuffer.allocate ( StaticVals.wLength_GET_ID );
            deviceId.order ( ByteOrder.BIG_ENDIAN );
            bsp.getDeviceID ( deviceId.array ( ) );

            if (bsp.IsErrorOccured ( )) {
                msg = "NBioBSP GetDeviceID Error: " + bsp.GetErrorCode ( );
                // txt.setText ( msg );
                return;
            }

            ByteBuffer setValue = ByteBuffer.allocate ( StaticVals.wLength_SET_VALUE );
            setValue.order ( ByteOrder.BIG_ENDIAN );

            byte[] src = new byte[StaticVals.wLength_SET_VALUE];
            for (int i = 0; i < src.length; i++) {
                src[i] = 1;
            }
            setValue.put ( src );
            bsp.setValue ( setValue.array ( ) );

            if (bsp.IsErrorOccured ( )) {
                msg = "NBioBSP SetValue Error: " + bsp.GetErrorCode ( );
                // txt.setText ( msg );
                return;
            }

            ByteBuffer getvalue = ByteBuffer.allocate ( StaticVals.wLength_GET_VALUE );
            getvalue.order ( ByteOrder.BIG_ENDIAN );
            bsp.getValue ( getvalue.array ( ) );

            if (bsp.IsErrorOccured ( )) {
                msg = "NBioBSP GetValue Error: " + bsp.GetErrorCode ( );
                // txt.setText ( msg );
                return;
            }
            src = new byte[StaticVals.wLength_SET_VALUE];
            System.arraycopy ( getvalue.array ( ), 0, src, 0, StaticVals.wLength_GET_VALUE );


            NBioBSPJNI.INIT_INFO_0 init_info_0 = bsp.new INIT_INFO_0 ( );
            bsp.GetInitInfo ( init_info_0 );

            NBioBSPJNI.CAPTURE_QUALITY_INFO mCAPTURE_QUALITY_INFO = bsp.new CAPTURE_QUALITY_INFO ( );
            bsp.GetCaptureQualityInfo ( mCAPTURE_QUALITY_INFO );

            mCAPTURE_QUALITY_INFO.EnrollCoreQuality = 70;
            mCAPTURE_QUALITY_INFO.EnrollFeatureQuality = 30;
            mCAPTURE_QUALITY_INFO.VerifyCoreQuality = 70;
            mCAPTURE_QUALITY_INFO.VerifyFeatureQuality = 30;
            bsp.SetCaptureQualityInfo ( mCAPTURE_QUALITY_INFO );





        }

        public int OnCaptured(final NBioBSPJNI.CAPTURED_DATA capturedData) {
            if (capturedData.getImageQuality ( ) >= QUALITY_LIMIT) {

                return NBioBSPJNI.ERROR.NBioAPIERROR_USER_CANCEL;
            } else if (capturedData.getDeviceError ( ) != NBioBSPJNI.ERROR.NBioAPIERROR_NONE) {

                return capturedData.getDeviceError ( );
            } else {
                return NBioBSPJNI.ERROR.NBioAPIERROR_NONE;
            }
        }
    };
}
