package au.com.bluedot.urbanairshipdemoapp;

import android.content.Context;
import android.widget.Toast;
import au.com.bluedot.point.net.engine.GeoTriggeringEventReceiver;
import au.com.bluedot.point.net.engine.ZoneEntryEvent;
import au.com.bluedot.point.net.engine.ZoneExitEvent;
import au.com.bluedot.point.net.engine.ZoneInfo;
import com.urbanairship.analytics.CustomEvent;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class AppGeoTriggerReceiver extends GeoTriggeringEventReceiver {
    private final String EVENT_PLACE_ENTERED = "bluedot_place_entered";
    private final String EVENT_PLACE_EXITED = "bluedot_place_exited";

    /**
     * This method is invoked whenever the set of zones is updated. There are a number of situations
     * when zone updates can happen, such as initialising the SDK, periodic update, significant location
     * change or zone sync event from Canvas.
     * @param zones List of zones associated with the projectId
     */
    @Override public void onZoneInfoUpdate(@NotNull List<ZoneInfo> zones, @NotNull Context context) {
        Toast.makeText(context, "Rules Updated",
                                  Toast.LENGTH_LONG).show();
    }

    /**
     * This method is invoked when the SDK registers an entry event into a geofeature.
     * There can be only one entry event per zone. However, after the minimum retrigger time lapses,
     * or a corresponding exit event occurs, the entry event may occur again.
     * @param entryEvent Provides details of the entry event.
     */
    @Override
    public void onZoneEntryEvent(@NotNull ZoneEntryEvent entryEvent, @NotNull Context context) {
        String entryDetails = "Entered zone "+entryEvent.getZoneInfo().getZoneName()+" via fence "+ entryEvent.getFenceInfo().getName();
        String customData = "";
        if(entryEvent.getZoneInfo().getCustomData() != null)
            customData = entryEvent.getZoneInfo().getCustomData().toString();
        Toast.makeText(context, entryDetails + customData,
                       Toast.LENGTH_LONG).show();
        sendCustomEvent(EVENT_PLACE_ENTERED, entryEvent.getZoneInfo(), entryEvent.getZoneInfo().getCustomData());
    }

    /**
     * This method is invoked when the SDK registers an exit event. An exit event can be triggered if
     * the geofeature is configured to trigger on exit. The option to enable exit events can be found
     * under project and zone configuration on Canvas. An exit event is a pending event and might occur
     * hours later after an entry event. Currently there is timeout for an exit of 24 hours. If an
     * exit wasn't triggered by that time, an automatic exit event will be registered.
     * @param exitEvent Provides details of the exit event.
     */
    @Override
    public void onZoneExitEvent(@NotNull ZoneExitEvent exitEvent, @NotNull Context context) {
        String exitDetails = "Exited zone" + exitEvent.getZoneInfo().getZoneName();
        String dwellT = "Dwell time: " + exitEvent.getDwellTime()+ " minutes";

        Toast.makeText(context, exitDetails + dwellT,
                                  Toast.LENGTH_LONG).show();
        sendCustomEvent(EVENT_PLACE_EXITED, exitEvent.getZoneInfo(), exitEvent.getDwellTime(), exitEvent.getZoneInfo().getCustomData());
    }

    private void sendCustomEvent(String eventName, ZoneInfo zoneInfo, Map<String, String> customDataMap) {
        sendCustomEvent(eventName, zoneInfo, -1, customDataMap);
    }

    private void sendCustomEvent(String eventName, ZoneInfo zoneInfo, int dwellTime, Map<String, String> customDataMap) {
        //        name: "bluedot_place_exited"
        //        interaction_type: "location"
        //        interaction_id: zone_id
        //        properties: {
        //            bluedot_zone_name: <zone_name>
        //                    dwell_time: <dwell_time>
        //  <all custom data>
        //        }
        CustomEvent.Builder builder = new CustomEvent.Builder(eventName);
        builder.setInteraction("location", zoneInfo.getZoneId());
        builder.addProperty("bluedot_zone_name", zoneInfo.getZoneName());
        if (customDataMap != null && !customDataMap.isEmpty()) {
            for (Map.Entry<String, String> data : customDataMap.entrySet()) {
                builder.addProperty(data.getKey(), data.getValue());
            }

        }


        if (dwellTime != -1) {
            builder.addProperty("dwell_time", dwellTime);
        }
        CustomEvent event = builder.build();

        System.out.println("-- event data : " + event.toJsonValue());
        event.track();
    }

}
