package eeinternship.com.timetracker;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import Data.TestClass;

/**
 * Created by IsakFe on 29. 03. 2017.
 */

public class StartWorkAdapter extends RecyclerView.Adapter<StartWorkAdapter.IViewHolder> {
    ArrayList<TestClass> adapter = new ArrayList<>();

    public StartWorkAdapter(ArrayList<TestClass> adapterC) {
        this.adapter = adapterC;
    }

    @Override
    public StartWorkAdapter.IViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_start_work, parent, false);
        IViewHolder iViewHolder = new IViewHolder(v);

        return iViewHolder;
    }

    @Override
    public void onBindViewHolder(IViewHolder holder, int position) {
        TestClass TC = adapter.get(position);
        holder.timeWork.setText(TC.getHour());
        holder.projectName.setText(TC.getProject());
    }

    @Override
    public int getItemCount() {
        return adapter.size();
    }

    public class IViewHolder extends RecyclerView.ViewHolder {
        TextView projectName, timeWork;

        public IViewHolder(View itemView) {
            super(itemView);
            projectName = (TextView) itemView.findViewById(R.id.project_name);
            timeWork = (TextView) itemView.findViewById(R.id.hour_min);
        }
    }
}