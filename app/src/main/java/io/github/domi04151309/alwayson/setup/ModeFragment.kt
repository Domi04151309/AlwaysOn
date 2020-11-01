package io.github.domi04151309.alwayson.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.activities.SetupActivity

class ModeFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_setup_mode, container, false)
        root.findViewById<RadioButton>(R.id.device_admin_mode).setOnClickListener {
            (context as SetupActivity).rootMode = false
        }
        root.findViewById<RadioButton>(R.id.root_mode).setOnClickListener {
            (context as SetupActivity).rootMode = true
        }
        return root
    }
}