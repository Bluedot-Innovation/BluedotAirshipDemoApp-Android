package io.bluedot.airshipdemo

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import io.bluedot.airshipdemo.R.id
import io.bluedot.airshipdemo.R.layout
import au.com.bluedot.point.net.engine.ServiceManager

class MainActivity : AppCompatActivity(), OnClickListener {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(layout.activity_main)
  }

  override fun onStart() {
    super.onStart()
    val init = findViewById<Button>(id.bInit)
    init.isEnabled = !ServiceManager.getInstance(applicationContext).isBluedotServiceInitialized
  }

  override fun onClick(v: View) {
    val ID = v.id
    val mainApplication: MainApplication = applicationContext as MainApplication
    when (ID) {
      id.bInit -> mainApplication.initPointSDK()
      id.bReset -> mainApplication.reset()
      else -> {}
    }
  }
}
