/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.example.android.bluetoothlegatt;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String CLIENT_DECRIPTOR_SHOOT_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_DECRIPTOR_READ_CONFIG = "98bfde16-fcda-4c99-9311-68799c8a75e6";

    public static String CHARACTERISTIC_TRIGGER_UUID = "756ad6a4-2007-4dc4-9173-72dc7d6b2627";
    public static String CHARACTERISTIC_IR_RECEIVE_UUID   = "a95980fb-4f18-4b2e-a258-81bf77575117";
    public static String CHARACTERISTIC_IR_SEND_UUID      = "8b91a0d2-5f7f-49cb-8939-4455d3d24b81";
    public static String CHARACTERISTIC_LATENCY_UUID      = "60e44cef-5a43-407b-8d1a-bce02377dcfd";
    public static String TaggerService = "08dbb28a-ce2c-467a-9f12-4f15d574a220";

    static {
        attributes.put(TaggerService, "Tagger");
        attributes.put(CHARACTERISTIC_TRIGGER_UUID, "Trigger");
        attributes.put(CHARACTERISTIC_IR_RECEIVE_UUID, "IR_RECEIVE");
        attributes.put(CHARACTERISTIC_IR_SEND_UUID, "IR_SEND");
        attributes.put(CHARACTERISTIC_LATENCY_UUID, "LATENCY");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
