package eeinternship.com.timetracker;

import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;

import Data.Ticket;
import Data.UserData;

/**
 * Created by IsakFe on 18. 04. 2017.
 */

public class newAdapter extends RecyclerSwipeAdapter<newAdapter.SimpleViewHolder> {
    private Context mContext;
    ArrayList<Ticket> adapter = new ArrayList<>();

    private ApplicationTimeTracker applicationTimeTracker;
    private UserData userData;
    ArrayList<Ticket> ticketArrayList;
    boolean enableStart = true;
    Integer indexCurrentTicket = -1;

    public newAdapter(StartWorkActivity startWorkActivity, ArrayList<Ticket> objects) {
        mContext = startWorkActivity;
        this.adapter = objects;
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_start_work, parent, false);
        newAdapter.SimpleViewHolder viewHolder = new newAdapter.SimpleViewHolder(view);

        return viewHolder;
    }

    public void swap(ArrayList<Ticket> tickets) {
        this.adapter = tickets;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final newAdapter.SimpleViewHolder holder, final int position) {
        final Ticket TC = adapter.get(position);

        holder.startWork = TC.getState();
        holder.projectName.setText(TC.getProject());
        if (TC.getColor() != null)
            holder.colorOfProject.setBackgroundColor(Color.parseColor(TC.getColor()));

        if (TC.getDescription() != null)
            holder.description.setText(TC.getDescription());
        else
            holder.description.setText("");


        if (holder.startWork == Ticket.State.Start) {
            holder.showTimer = true;
            holder.imageButton.setBackgroundResource(R.drawable.img_start_btn);
            TC.setTime("0:00");
        } else if (holder.startWork == Ticket.State.Stop)
            holder.imageButton.setBackgroundResource(R.drawable.img_stop_btn);
        else if (holder.startWork == Ticket.State.Restart)
            holder.imageButton.setBackgroundResource(R.drawable.img_recreate_btn);
        else if (holder.startWork == Ticket.State.Done)
            holder.imageButton.setBackgroundResource(R.drawable.img_finish_btn);

        holder.timeWork.setText(TC.getTime());
        holder.imageButton.setVisibility(View.VISIBLE);

        if (!holder.enableStart) {
            holder.imageButton.setEnabled(false);
        }


        final CountDownTimer projectTimeTracker = new CountDownTimer(1000000000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                Calendar current = Calendar.getInstance();
                Time diff = new Time(current.get(Calendar.HOUR_OF_DAY), current.get(Calendar.MINUTE), current.get(Calendar.SECOND));
                long difference = diff.getTime() - holder.startTime.getTime();
                Time differenceTime = new Time(difference);
                int workTimeHours = differenceTime.getHours() - 1;
                int workTimeMinutes = differenceTime.getMinutes();
                setTextView(workTimeHours, workTimeMinutes, holder.timeWork, holder.showTimer);
            }

            @Override
            public void onFinish() {

            }
        };
        holder.imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();

                if (holder.startWork == Ticket.State.Start && TC.getStateStart() == true) {
                    holder.startTime = new Time(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
                    holder.startWork = Ticket.State.Stop;
                    TC.setDate(calendar);
                    holder.imageButton.setBackgroundResource(R.drawable.img_stop_btn);
                    projectTimeTracker.start();
                    TC.setStartingTime(holder.startTime);
                    TC.setState(holder.startWork);
                    updateTicketState(position);
                    indexCurrentTicket = position;
                    adapter.set(position, TC);

                } else if (holder.startWork == Ticket.State.Stop) {
                    holder.showTimer = false;
                    projectTimeTracker.cancel();
                    holder.finishTime = new Time(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
                    TC.setFinishTime(holder.finishTime);
                    if (holder.startTime == null) {
                        holder.startTime = TC.getStartingTime();
                    }
                    long differenceLong = holder.finishTime.getTime() - holder.startTime.getTime();
                    Time workTime = new Time(differenceLong);
                    if (workTime.getMinutes() < 10)
                        holder.timeWork.setText(workTime.getHours() - 1 + ":0" + workTime.getMinutes());
                    else
                        holder.timeWork.setText(workTime.getHours() - 1 + ":" + workTime.getMinutes());
                    String timeWork = holder.timeWork.getText().toString();
                    holder.imageButton.setBackgroundResource(R.drawable.img_recreate_btn);
                    holder.startWork = Ticket.State.Restart;
                    TC.setTime(timeWork);
                    TC.setState(holder.startWork);
                    updateTicketState(-1);
                    indexCurrentTicket = -1;
                    TC.setStateStart(true);
                    adapter.set(position, TC);
                } else if (holder.startWork == Ticket.State.Restart) {
                    holder.imageButton.setBackgroundResource(R.drawable.img_finish_btn);
                    adapter.add(0, new Ticket("0:00", TC.getProject(), Ticket.State.Start, TC.getSelected(), TC.getColor()));
                    notifyDataSetChanged();
                    updateTicketState(indexCurrentTicket);
                    holder.startWork = Ticket.State.Done;
                    TC.setState(holder.startWork);
                    adapter.set(position + 1, TC);
                }else if(TC.getStateStart()==false) {
                    //Toast.makeText(mContext,"Another ticket is still running.",Toast.LENGTH_LONG).show();
                    LayoutInflater inflater = ((StartWorkActivity) mContext).getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom_dialog_alert,
                            (ViewGroup)((StartWorkActivity) mContext).findViewById(R.id.custom_toast_container));

                    TextView text = (TextView) layout.findViewById(R.id.text);
                    text.setText("Another ticket is still running. ");

                    Toast toast = new Toast(mContext);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                }

                ((StartWorkActivity)mContext).hideSoftKeyboard();
                ((StartWorkActivity)mContext).clearFocus();

                userData.setTicketList(adapter);
                applicationTimeTracker.setUserData(userData);
            }
        });


        holder.description.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!holder.description.getText().toString().equals("")) {
                    TC.setDescription(holder.description.getText().toString());
                    adapter.set(position, TC);
                    userData.setTicketList(adapter);
                    applicationTimeTracker.setUserData(userData);
                }
            }
        });


        applicationTimeTracker = (ApplicationTimeTracker) ((StartWorkActivity) mContext).getApplication();
        userData = applicationTimeTracker.getUserData();
        ticketArrayList = userData.getTicketList();

        holder.swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        holder.swipeLayout.addDrag(SwipeLayout.DragEdge.Left, holder.swipeLayout.findViewById(R.id.bottom_wrapper1));
        holder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, holder.swipeLayout.findViewById(R.id.bottom_wrapper));

        holder.swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onClose(SwipeLayout layout) {
                //when the SurfaceView totally cover the BottomView.
                ((StartWorkActivity)mContext).hideSoftKeyboard();
                ((StartWorkActivity)mContext).clearFocus();
            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                //you are swiping.
            }

            @Override
            public void onStartOpen(SwipeLayout layout) {

            }

            @Override
            public void onOpen(SwipeLayout layout) {
                //when the BottomView totally show.
                ((StartWorkActivity)mContext).hideSoftKeyboard();
                ((StartWorkActivity)mContext).clearFocus();
            }

            @Override
            public void onStartClose(SwipeLayout layout) {
            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
            }
        });

        holder.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Ticket currentTicket = adapter.get(position);
                if (currentTicket.getFinishTime() == null) {
                    Calendar calendar = Calendar.getInstance();
                    currentTicket.setFinishTime(new Time(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND)));
                }
                if (currentTicket.getDate() == null) {
                    //Toast.makeText(mContext, "You did not start this ticket!", Toast.LENGTH_LONG).show();

                    LayoutInflater inflater = ((StartWorkActivity) mContext).getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom_dialog_alert,
                            (ViewGroup)((StartWorkActivity) mContext).findViewById(R.id.custom_toast_container));

                    TextView text = (TextView) layout.findViewById(R.id.text);
                    text.setText("You didn't start this ticket. ");

                    Toast toast = new Toast(mContext);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                    mItemManger.closeAllItems();
                    return;
                }
                if (currentTicket.getDescription() == null || currentTicket.getDescription().equals("")) {
                    //Toast.makeText(mContext, "You did not write Description!", Toast.LENGTH_LONG).show();
                    LayoutInflater inflater = ((StartWorkActivity) mContext).getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom_dialog_alert,
                            (ViewGroup)((StartWorkActivity) mContext).findViewById(R.id.custom_toast_container));

                    TextView text = (TextView) layout.findViewById(R.id.text);
                    text.setText("You didn't write description. ");

                    Toast toast = new Toast(mContext);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                    mItemManger.closeAllItems();
                    return;
                }

                if(TC.getStateStart()==false){
                    currentTicket.setStateStart(true);
                    ticketArrayList.set(position,currentTicket);
                    TC.setStateStart(true);
                    updateTicketState(-1);
                }
                applicationTimeTracker.addWorkOn(mContext, userData.getUserAcount(), currentTicket);

                mItemManger.removeShownLayouts(holder.swipeLayout);
                adapter.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, ticketArrayList.size());
                userData.setTicketList(ticketArrayList);
                applicationTimeTracker.setUserData(userData);
                mItemManger.closeAllItems();

               // Toast.makeText(mContext, "Ticket successfully sent!", Toast.LENGTH_LONG).show();

                LayoutInflater inflater = ((StartWorkActivity) mContext).getLayoutInflater();
                View layout = inflater.inflate(R.layout.custom_dialog,
                        (ViewGroup)((StartWorkActivity) mContext).findViewById(R.id.custom_toast_container));

                TextView text = (TextView) layout.findViewById(R.id.text);
                    text.setText("Ticket successfully sent. ");

                Toast toast = new Toast(mContext);
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setView(layout);
                toast.show();

               /* Intent intent = ((StartWorkActivity) mContext).getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                ((StartWorkActivity) mContext).finish();
                mContext.startActivity(intent);
                ((StartWorkActivity) mContext).overridePendingTransition(0, 0);*/

            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TC.getStateStart()==false){
                    adapter.get(position).setStateStart(true);
                    ticketArrayList.set(position,adapter.get(position));
                    TC.setStateStart(true);
                    updateTicketState(-1);
                }

                mItemManger.removeShownLayouts(holder.swipeLayout);
                ticketArrayList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, ticketArrayList.size());
                userData.setTicketList(ticketArrayList);
                applicationTimeTracker.setUserData(userData);
                mItemManger.closeAllItems();
                ((StartWorkActivity) mContext).openFabButtonWhenDelete();

                LayoutInflater inflater = ((StartWorkActivity) mContext).getLayoutInflater();
                View layout = inflater.inflate(R.layout.custom_dialog_red,
                        (ViewGroup)((StartWorkActivity) mContext).findViewById(R.id.custom_toast_container));

                TextView text = (TextView) layout.findViewById(R.id.text);
                text.setText("Ticket successfully deleted. ");

                Toast toast = new Toast(mContext);
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setView(layout);
                toast.show();
            }
        });
        mItemManger.bindView(holder.itemView, position);


    }

    public void setTextView(int hours, int minutes, TextView textView, boolean doWork) {
        if (!doWork)
            return;

        if (minutes < 10)
            textView.setText(hours + ":0" + minutes);
        else
            textView.setText(hours + ":" + minutes);
    }

    @Override
    public int getItemCount() {
        return adapter.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }
    public void updateTicketState(int index) {
        if (index == -1) {
            for (int i = 0; i < adapter.size(); i++) {
                if (i != index) {
                    adapter.get(i).setStateStart(true);
                }
            }
        } else {
            for (int i = 0; i < adapter.size(); i++) {
                adapter.get(i).setStateStart(false);
            }
        }
    }


    public class SimpleViewHolder extends RecyclerView.ViewHolder {
        SwipeLayout swipeLayout;
        TextView projectName, timeWork;
        ImageButton delete, send;
        EditText description;
        ImageButton imageButton;
        Time startTime;
        Time finishTime;
        LinearLayout colorOfProject;
        Ticket.State startWork = Ticket.State.Start;
        boolean showTimer = true;
        boolean enableStart = true;

        public SimpleViewHolder(View itemView) {
            super(itemView);
            send = (ImageButton) itemView.findViewById(R.id.btnSend);
            delete = (ImageButton) itemView.findViewById(R.id.tvDelete);
            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);
            projectName = (TextView) itemView.findViewById(R.id.project_name);
            timeWork = (TextView) itemView.findViewById(R.id.hour_min);
            imageButton = (ImageButton) itemView.findViewById(R.id.btn_start_work);
            description = (EditText) itemView.findViewById(R.id.desc_text);
            if (startWork == Ticket.State.Start)
                imageButton.setBackgroundResource(R.drawable.img_start_btn);
            colorOfProject = (LinearLayout) itemView.findViewById(R.id.color_of_project);
        }
    }
}