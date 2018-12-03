package zandoh.com.polyview

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_login.*
import android.content.Context.MODE_PRIVATE
import android.arch.lifecycle.ViewModelProviders
import android.support.v4.widget.DrawerLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*


class LoginActivity: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_login, container, false)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        login_button.setOnClickListener {

            login_button.isEnabled = false
            email.isEnabled = false
            password.isEnabled = false
            progressBar.visibility = View.VISIBLE

            val email_text = email.text.toString()
            val password_text = password.text.toString()

            val editor = activity?.getPreferences(MODE_PRIVATE)?.edit()
            editor?.putString("username", email_text)
            editor?.putString("password", password_text)
            editor?.apply()

            val model = ViewModelProviders.of(it.context as MainActivity).get(PolylearnModel::class.java)
            model.username = email_text
            model.password = password_text

            val provider = (activity as MainActivity).getDataProvider()
            provider.collectData(email_text, password_text) {
                fragmentManager?.beginTransaction()
                        ?.replace(R.id.fragment, ClassesActivity())
                        ?.commit()
            }
        }
    }
}