package aegismatrix.com.namm_radio;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * Created by Dhiraj on 31-03-2017.
 */

public class ProgramRVAdapter extends RecyclerView.Adapter<ProgramScheduleViewHolder> {

    private LayoutInflater layoutInflater;
    private List<ProgramItemSchedulerModel> programItemSchedulerModels = Collections.emptyList();

    public ProgramRVAdapter(Context inContext, List<ProgramItemSchedulerModel> inProgramItemSchedulerModels) {
        layoutInflater = LayoutInflater.from(inContext);
        this.programItemSchedulerModels = inProgramItemSchedulerModels;
    }

    @Override
    public ProgramScheduleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View rowView = layoutInflater.inflate(R.layout.program_schedule_item, parent, false);
        ProgramScheduleViewHolder programScheduleViewHolder = new ProgramScheduleViewHolder(rowView);
        return programScheduleViewHolder;
    }

    @Override
    public void onBindViewHolder(ProgramScheduleViewHolder holder, int position) {
        ProgramItemSchedulerModel programItemSchedulerModel = programItemSchedulerModels.get(position);
        holder.programName.setText(programItemSchedulerModel.getProgramName());
        holder.hostName.setText(programItemSchedulerModel.getHostName());
        holder.showTimings.setText(programItemSchedulerModel.getShowTimings());
        holder.equilizer.setImageDrawable(programItemSchedulerModel.getEquilizer());
    }

    @Override
    public int getItemCount() {
        return programItemSchedulerModels.size();
    }
}

/**
 * View Holder
 */
class ProgramScheduleViewHolder extends RecyclerView.ViewHolder {
    protected TextView programName, hostName, showTimings;
    protected ImageView equilizer;

    public ProgramScheduleViewHolder(View itemView) {
        super(itemView);
        programName = (TextView) itemView.findViewById(R.id.programName);
        hostName = (TextView) itemView.findViewById(R.id.hostName);
        showTimings = (TextView) itemView.findViewById(R.id.programTimings);
        equilizer = (ImageView) itemView.findViewById(R.id.activeShowMusicBar);
        RelativeLayout relativeLayout = (RelativeLayout) itemView.findViewById(R.id.itemLayout);
        relativeLayout.getBackground().setAlpha(128);
    }
}