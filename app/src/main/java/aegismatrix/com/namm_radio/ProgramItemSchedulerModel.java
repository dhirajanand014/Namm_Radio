package aegismatrix.com.namm_radio;

import android.graphics.drawable.BitmapDrawable;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Dhiraj on 31-03-2017.
 */

public class ProgramItemSchedulerModel implements Parcelable {
    private String programName;
    private String hostName;
    private String showTimings;

    public ProgramItemSchedulerModel() {
    }

    protected ProgramItemSchedulerModel(Parcel in) {
        programName = in.readString();
        hostName = in.readString();
        showTimings = in.readString();
    }

    public static final Creator<ProgramItemSchedulerModel> CREATOR = new Creator<ProgramItemSchedulerModel>() {
        @Override
        public ProgramItemSchedulerModel createFromParcel(Parcel in) {
            return new ProgramItemSchedulerModel(in);
        }

        @Override
        public ProgramItemSchedulerModel[] newArray(int size) {
            return new ProgramItemSchedulerModel[size];
        }
    };

    public BitmapDrawable getEquilizer() {
        return equilizer;
    }

    public void setEquilizer(BitmapDrawable equilizer) {
        this.equilizer = equilizer;
    }

    private BitmapDrawable equilizer;

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getShowTimings() {
        return showTimings;
    }

    public void setShowTimings(String showTimings) {
        this.showTimings = showTimings;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(programName);
        dest.writeString(hostName);
        dest.writeString(showTimings);
    }
}
