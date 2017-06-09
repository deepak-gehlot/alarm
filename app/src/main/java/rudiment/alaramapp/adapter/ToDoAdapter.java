package rudiment.alaramapp.adapter;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidquery.AQuery;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cn.iwgang.countdownview.CountdownView;
import rudiment.alaramapp.R;
import rudiment.alaramapp.bean.ReminderBean;
import rudiment.alaramapp.dao.QueryManager;
import rudiment.alaramapp.util.Callback;
import rudiment.alaramapp.util.Constant;
import rudiment.alaramapp.util.DialogListener;
import rudiment.alaramapp.util.OnItemClickListener;
import rudiment.alaramapp.util.PreferenceConnector;
import rudiment.alaramapp.util.Utility;

import static rudiment.alaramapp.util.PreferenceConnector.USER_Id;

/**
 * Created by RWS 6 on 2/6/2017.
 */
public class ToDoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<ReminderBean.ReminderItem> reminderItems;
    private AQuery aQuery;
    private Context context;
    OnItemClickListener itemClickListener;
    ArrayList<String> imageList = new ArrayList<>();

    public ToDoAdapter(Context context, ArrayList<ReminderBean.ReminderItem> reminderItems) {
        this.reminderItems = reminderItems;
        aQuery = new AQuery(context);
        this.context = context;
        getAllImages(reminderItems);
    }

    class ItemHolder extends RecyclerView.ViewHolder {

        final View view;
        final ImageView mReminderImg;
        final TextView mDescriptionTxt;
        CountdownView mCountdownView;
        LinearLayout crossItOutLayout;

        public ItemHolder(View view) {
            super(view);
            this.view = view;
            mReminderImg = (ImageView) view.findViewById(R.id.remiderImg);
            mDescriptionTxt = (TextView) view.findViewById(R.id.descriptionTxt);
            mCountdownView = (CountdownView) view.findViewById(R.id.countdownView);
            crossItOutLayout = (LinearLayout) view.findViewById(R.id.crossItOutLayout);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.to_do_row, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder1, final int position) {
        ItemHolder holder = (ItemHolder) holder1;
        final ReminderBean.ReminderItem reminderItem = reminderItems.get(position);
        aQuery.id(holder.mReminderImg).image(reminderItem.reminder_image, true, true, 70, R.drawable.no_image_available);
        holder.mDescriptionTxt.setText(reminderItem.reminder_description);
        holder.mCountdownView.start(getTimeInMilliSeconds(makeDateTimeString(reminderItem.reminder_date, reminderItem.reminder_time)));

        holder.mReminderImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDialog(context, reminderItem.reminder_image, position);
            }
        });

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemClickListener.onItemClick(position, reminderItem);
            }
        });

        holder.crossItOutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setConfirmationDialog(position);
            }
        });
    }

    private void setConfirmationDialog(final int position) {
        Utility.setDialog(context, context.getString(R.string.alert), "Are you sure, Do you want to cross it out.", "Cancel", "Yes", new DialogListener() {
            @Override
            public void onNegative(DialogInterface dialog) {
                dialog.dismiss();
            }

            @Override
            public void onPositive(DialogInterface dialog) {
                dialog.dismiss();
                moveReminderToHistory(position, reminderItems.get(position).reminder_id);
            }
        });
    }

    /**
     * move reminder to history list (Cross It Out)
     *
     * @param position
     * @param reminder_id
     */
    private void moveReminderToHistory(final int position, String reminder_id) {
        final ProgressDialog progressDialog = ProgressDialog.show(context, "", "wait...", false, false);
        try {
            JSONStringer jsonStringer = new JSONStringer().object()
                    .key("user_id").value(PreferenceConnector.readString(context, USER_Id, ""))
                    .key("reminder_id").value(reminder_id)
                    .endObject();
            QueryManager.getInstance().postRequest(Constant.URL + "add_reminder_history.php", jsonStringer.toString(), new Callback() {
                @Override
                public void onResult(String result) {
                    progressDialog.dismiss();
                    if (result != null && !result.isEmpty()) {
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.getString("responseCode").equals("200")) {
                                reminderItems.remove(position);
                                notifyItemRemoved(position);
                            } else {
                                Utility.showToast(context, jsonObject.getString("responseMessage"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Utility.showToast(context, context.getString(R.string.wrong));
                        }
                    } else {
                        Utility.showToast(context, context.getString(R.string.wrong));
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            progressDialog.dismiss();
            Utility.showToast(context, context.getString(R.string.wrong));
        }
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public int getItemCount() {
        return reminderItems.size();
    }

    private String makeDateTimeString(String date, String time) {
        return date + " " + time;
    }

    private long getTimeInMilliSeconds(String future) {
        Long timeDiff = 0l;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date futureDate = dateFormat.parse(future);

            String currentDate = dateFormat.format(new Date());
            Date cDate = dateFormat.parse(currentDate);
            timeDiff = futureDate.getTime() - cDate.getTime();
            return timeDiff;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeDiff;
    }

    private void getAllImages(ArrayList<ReminderBean.ReminderItem> reminderItems) {
        if (reminderItems != null && reminderItems.size() > 0) {
            for (int i = 0; i < reminderItems.size(); i++) {
                final ReminderBean.ReminderItem reminderItem = reminderItems.get(i);
                String s = reminderItem.reminder_image;
                imageList.add(s);
            }
        }
    }

    private void setDialog(Context context, String url, int position) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.view_full_image_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ImageView closeBtn = (ImageView) dialog.findViewById(R.id.closeBtn);
        ViewPager viewPager = (ViewPager) dialog.findViewById(R.id.view_pager);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();

        ViewPagerAdapter adapterView = new ViewPagerAdapter(context, imageList);
        viewPager.setAdapter(adapterView);
        viewPager.setCurrentItem(position);
    }

    public class ViewPagerAdapter extends PagerAdapter {
        Context mContext;
        ArrayList<String> mList = new ArrayList<>();

        ViewPagerAdapter(Context context, ArrayList<String> list) {
            this.mContext = context;
            this.mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public boolean isViewFromObject(View v, Object obj) {
            return v == ((ImageView) obj);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int i) {
            ImageView mImageView = new ImageView(mContext);
            //mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //mImageView.setImageResource(mList.get(i));
            AQuery aQuery = new AQuery(context);
            aQuery.id(mImageView).image(mList.get(i), true, true, 0, R.drawable.no_image_available);
            ((ViewPager) container).addView(mImageView, 0);
            return mImageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int i, Object obj) {
            ((ViewPager) container).removeView((ImageView) obj);
        }
    }
}
