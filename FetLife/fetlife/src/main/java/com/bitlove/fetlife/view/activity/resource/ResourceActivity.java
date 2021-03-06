package com.bitlove.fetlife.view.activity.resource;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import com.bitlove.fetlife.R;
import com.bitlove.fetlife.event.AuthenticationFailedEvent;
import com.bitlove.fetlife.event.ServiceCallFailedEvent;
import com.bitlove.fetlife.event.ServiceCallFinishedEvent;
import com.bitlove.fetlife.event.ServiceCallStartedEvent;
import com.bitlove.fetlife.view.activity.BaseActivity;
import com.bitlove.fetlife.view.activity.standalone.LoginActivity;
import com.bitlove.fetlife.view.activity.component.EventDisplayHandler;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public abstract class ResourceActivity extends BaseActivity {

    private static final String PREFERENCE_VERSION_NOTIFICATION_INT = "ResourceActivity.PREFERENCE_VERSION_NOTIFICATION_INT";

    private EventDisplayHandler eventDisplayHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        eventDisplayHandler = new EventDisplayHandler();

        if (verifyUser()) {
            onResourceCreate(savedInstanceState);
            showVersionSnackBarIfNeeded();
        }

        TextView text = (TextView)findViewById(R.id.text_preview);

        RotateAnimation rotate= (RotateAnimation) AnimationUtils.loadAnimation(this,R.anim.preview_rotation);
        text.setAnimation(rotate);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (verifyUser()) {
            onResourceStart();
        }
    }

    protected abstract void onResourceCreate(Bundle savedInstanceState);

    protected abstract void onResourceStart();

    protected boolean verifyUser() {
        if (getFetLifeApplication().getUserSessionManager().getCurrentUser() == null) {
            LoginActivity.startLogin(getFetLifeApplication());
            finish();
            return false;
        }
        return true;
    }

    private void showVersionSnackBarIfNeeded() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int lastVersionNotification = preferences.getInt(PREFERENCE_VERSION_NOTIFICATION_INT, 0);
        int versionNumber = getFetLifeApplication().getVersionNumber();
        if (lastVersionNotification < versionNumber) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), getString(R.string.snackbar_version_notification, getFetLifeApplication().getVersionText()), Snackbar.LENGTH_LONG);
            snackbar.getView().setBackgroundColor(getResources().getColor(R.color.color_accent_light));
            TextView snackText = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
            snackText.setTextColor(getResources().getColor(R.color.color_accent));
            snackText.setTypeface(null, Typeface.BOLD);
            snackbar.show();
            preferences.edit().putInt(PREFERENCE_VERSION_NOTIFICATION_INT, versionNumber).apply();
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAuthenticationFailed(AuthenticationFailedEvent authenticationFailedEvent) {
        eventDisplayHandler.onAuthenticationFailed(this, authenticationFailedEvent);
        getFetLifeApplication().getUserSessionManager().onUserLogOut();
        LoginActivity.startLogin(getFetLifeApplication());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServiceCallFailed(ServiceCallFailedEvent serviceCallFailedEvent) {
        eventDisplayHandler.onServiceCallFailed(this, serviceCallFailedEvent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServiceCallFinished(ServiceCallFinishedEvent serviceCallFinishedEvent) {
        eventDisplayHandler.onServiceCallFinished(this, serviceCallFinishedEvent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServiceCallStarted(ServiceCallStartedEvent serviceCallStartedEvent) {
        eventDisplayHandler.onServiceCallStarted(this, serviceCallStartedEvent);
    }
}
