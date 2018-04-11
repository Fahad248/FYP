package com.fyp_lubdub;

import android.content.Intent;
import android.os.Bundle;

interface Interface_MainActivity {
    void onCreate(Bundle savedInstanceState);

    void QA();

    float[] transform(float[] s);

    void onActivityResult(int requestCode, int resultCode, Intent data);
}
