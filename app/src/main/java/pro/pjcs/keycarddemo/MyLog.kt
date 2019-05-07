package pro.pjcs.keycarddemo

import android.util.Log

class MyLog {

    companion object {

        fun v(tag : String, message: String){
            if(BuildConfig.DEBUG) Log.v(tag, message)
        }

        fun i(tag : String, message: String){
            if(BuildConfig.DEBUG) Log.i(tag, message)
        }

        fun w(tag : String, message: String){
            if(BuildConfig.DEBUG) Log.w(tag, message)
        }

        fun e(tag : String, message: String){
            if(BuildConfig.DEBUG) Log.e(tag, message)
        }

    }

}