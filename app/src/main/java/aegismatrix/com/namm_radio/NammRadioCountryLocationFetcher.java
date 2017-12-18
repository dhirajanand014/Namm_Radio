package aegismatrix.com.namm_radio;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

/**
 * Created by Dhiraj on 16-05-2017.
 */
public class NammRadioCountryLocationFetcher extends IntentService {

    private String errorMessage = "";
    protected ResultReceiver mReceiver;

    public NammRadioCountryLocationFetcher() {
        super("NammRadioCountryLocationFetcher");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        Location location = intent.getParcelableExtra(GlobalConstants.LOCATION_DATA_EXTRA);
        mReceiver = intent.getParcelableExtra(GlobalConstants.RECEIVER);
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            errorMessage = getString(R.string.service_not_available);
        } catch (IllegalArgumentException illegalArgumentException) {
            errorMessage = getString(R.string.invalid_lat_long_used);
        }
        // Handle case where no address was found.
        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found);
                Log.e(TAG, errorMessage);
            }
            deliverResultToReceiver(GlobalConstants.FAILURE_RESULT, errorMessage);
        } else {
            String countryCode = addresses.get(0).getCountryCode().toUpperCase();
            if (countryCode != null) {
                if (Arrays.asList(GlobalConstants.GULF_CONTINENTS).contains(countryCode)) {
                    countryCode = GlobalConstants.STATIONS[2];
                } else if (countryCode.equalsIgnoreCase(GlobalConstants.STATIONS[1])) {
                    countryCode = GlobalConstants.STATIONS[1];
                } else if (countryCode.startsWith(GlobalConstants.STATIONS[0])) {
                    countryCode = GlobalConstants.STATIONS[0];
                } else {
                    countryCode = getNearestCountryCode(location, countryCode);
                }
            }
            deliverResultToReceiver(GlobalConstants.SUCCESS_RESULT, countryCode);
        }
    }

    private String getNearestCountryCode(Location location, String countryCode) {
        float indiaDistance = location.distanceTo(getLocationLAtLon(20.593684, 78.962880));
        float gulfDistance = location.distanceTo(getLocationLAtLon(26.7505, 51.6834));
        float usaDistance = location.distanceTo(getLocationLAtLon(37.0902, 95.7129));
        if (indiaDistance < gulfDistance && indiaDistance < usaDistance) {
            countryCode = GlobalConstants.STATIONS[0];
        } else if (gulfDistance < usaDistance && gulfDistance < indiaDistance) {
            countryCode = GlobalConstants.STATIONS[2];
        } else {
            countryCode = GlobalConstants.STATIONS[1];
        }
        return countryCode;
    }


    @NonNull
    private Location getLocationLAtLon(double inLatitude, double inLogitude) {
        Location location = new Location("abc");
        location.setLatitude(inLatitude);
        location.setLongitude(inLogitude);
        return location;
    }

    private void deliverResultToReceiver(int failureResult, String countryCode) {
        Bundle bundle = new Bundle();
        bundle.putString(GlobalConstants.RESULT_DATA_KEY, countryCode);
        mReceiver.send(failureResult, bundle);
    }
}
