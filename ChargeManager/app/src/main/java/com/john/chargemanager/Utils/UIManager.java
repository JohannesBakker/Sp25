package com.john.chargemanager.Utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.john.chargemanager.R;

public class UIManager {
    private static UIManager _instance;
    protected Context mContext;
    private ProgressDialog progressDialog = null;

    public static UIManager initialize(Context context) {
        if (_instance == null)
            _instance = new UIManager(context);
        return _instance;
    }

    public static UIManager sharedInstance() {
        return _instance;
    }

    private UIManager(Context context) {
        mContext = context;
    }

    public Object showProgressDialog(Context context, String title, String message, boolean indeterminate) {
        try {
            if( progressDialog != null && progressDialog.isShowing() )
                progressDialog.dismiss();
            progressDialog = ProgressDialog.show((context == null ? mContext : context), title, message, indeterminate);
        } catch (Exception e) {
            Log.e("ProgressDialog", e.getMessage());
            return null;
        }

        return progressDialog;

    }

    public void dismissProgressDialog() {
        dismissProgressDialog(progressDialog);
    }

    public void dismissProgressDialog(Object dlg) {
        if (dlg == null || !(dlg instanceof ProgressDialog) )
            return;

        ProgressDialog progressDialog = (ProgressDialog)dlg;
        if(progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    public static Toast gToast = null;
    public void showToastMessage(Context context, String message) {
        if (gToast == null || gToast.getView().getWindowVisibility() != View.VISIBLE) {
            gToast = Toast.makeText((context == null) ? mContext : context, message, Toast.LENGTH_LONG);
            gToast.setGravity(Gravity.CENTER, 0, 0);
            gToast.show();
        }
    }

    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if( listAdapter == null ) {
            return;
        }

        int totalHeight = 0;
        for( int i = 0; i < listAdapter.getCount(); i++ ) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    /*
    public void showAdvancedToast(String message, int gravity)
    {
        //Log.d(TAG,"showAdvanceToast");
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View view = inflater.inflate(R.layout.custom_toast, null);

        TextView txtView = (TextView)view.findViewById(R.id.txt_message);
        txtView.setText(message);
        showToastWithView(view, gravity);
    }

    private void showToastWithView(View view, int gravity)
    {
        if (gToast != null)
            gToast.cancel();

        gToast = new Toast(mContext);
        gToast.setView(view);

        Point ptSize = ResolutionSet.getScreenSize(mContext, false, true);
        // ptSize.y -= statusBarHeight(activity);
        ResolutionSet._instance.iterateChild(view);

        gToast.setDuration(Toast.LENGTH_SHORT);
        if ((gravity & Gravity.BOTTOM) == Gravity.BOTTOM)
            gToast.setGravity(gravity, 0, ptSize.y * 1 / 5);
        else
            gToast.setGravity(gravity, 0, 0);
        gToast.show();
    }
    */

    /**
     * Get resource name with screen density suffix
     *
     * @param resourceName  Name of the resource
     * @return              Returns the resource name with screen density suffix
     */
    public String getDrawableResourceNameWithScreenDensitySuffix(String resourceName) {
        return resourceName + "_" + ResolutionSet.getScreenDensityString(mContext) + ".png";
    }

    public String getDrawableResourceNameWithScreenDensitySuffix(String resourceName, String extension) {
        return resourceName + "_" + ResolutionSet.getScreenDensityString(mContext) + "." + extension;
    }

    /**
     * Show and hide with fade in and out animation
     *
     * @param view      View to be animated
     */
    public void showToastAnimation(final View view) {
        Animation fadeInAnimation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);

        fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Animation fadeOutAnimation = AnimationUtils.loadAnimation(mContext, R.anim.fade_out);
                        fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                view.setVisibility(View.INVISIBLE);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        view.startAnimation(fadeOutAnimation);
                    }
                }, 1500);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        view.startAnimation(fadeInAnimation);
    }
}
