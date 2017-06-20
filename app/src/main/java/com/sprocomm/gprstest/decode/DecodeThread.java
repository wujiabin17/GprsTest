/*
 * Copyright (C) 2008 ZXing authors
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

package com.sprocomm.gprstest.decode;

import android.os.Handler;
import android.os.Looper;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.ResultPointCallback;
import com.sprocomm.gprstest.CaptureActivity;
import com.sprocomm.gprstest.libs.Config;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * This thread does all the heavy lifting of decoding the images.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
final class DecodeThread extends Thread
{

    public static final String BARCODE_BITMAP = "barcode_bitmap";

    private final CaptureActivity activity;
    private final Map<DecodeHintType, Object> hints;
    private Handler handler;
    private final CountDownLatch handlerInitLatch;

    DecodeThread(CaptureActivity activity, Collection<BarcodeFormat> decodeFormats, String characterSet, ResultPointCallback resultPointCallback)
    {

        this.activity = activity;
        handlerInitLatch = new CountDownLatch(1);

        hints = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);

        // The prefs can't change while the thread is running, so pick them up once here.
        if (decodeFormats == null || decodeFormats.isEmpty ())
        {
            decodeFormats = EnumSet.noneOf (BarcodeFormat.class);
            if (Config.KEY_DECODE_1D)
            {
                decodeFormats.addAll (DecodeFormatManager.ONE_D_FORMATS);
            }
            if (Config.KEY_DECODE_QR)
            {
                decodeFormats.addAll (DecodeFormatManager.QR_CODE_FORMATS);
            }
            if (Config.KEY_DECODE_DATA_MATRIX)
            {
                decodeFormats.addAll (DecodeFormatManager.DATA_MATRIX_FORMATS);
            }
        }
        hints.put (DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

        if (characterSet != null)
        {
            hints.put (DecodeHintType.CHARACTER_SET, characterSet);
        }
        hints.put (DecodeHintType.NEED_RESULT_POINT_CALLBACK, resultPointCallback);
    }

    Handler getHandler(){
        try
        {
            handlerInitLatch.await ();
        } catch (InterruptedException ie)
        {
            // continue?
        }
        return handler;
    }

    @Override
    public void run(){
        Looper.prepare ();
        handler = new DecodeHandler(activity,hints);
        handlerInitLatch.countDown ();
        Looper.loop ();
    }

}
