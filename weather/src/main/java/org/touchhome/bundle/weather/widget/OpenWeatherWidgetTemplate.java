package org.touchhome.bundle.weather.widget;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.ui.field.UIField;
import org.touchhome.bundle.api.ui.field.UIFieldNumber;
import org.touchhome.bundle.api.ui.field.UIFieldType;
import org.touchhome.bundle.api.util.TouchHomeUtils;
import org.touchhome.bundle.api.widget.JavaScriptBuilder;
import org.touchhome.bundle.api.widget.WidgetJSBaseTemplate;
import org.touchhome.bundle.weather.setting.WeatherApiKeySetting;
import org.touchhome.bundle.weather.setting.WeatherUnitSetting;

@Getter
@Component
@RequiredArgsConstructor
public class OpenWeatherWidgetTemplate implements WidgetJSBaseTemplate {
    private final EntityContext entityContext;

    @UIField(order = 1, type = UIFieldType.Slider, label = "weather.type")
    @UIFieldNumber(min = 1, max = 24)
    private Integer id;

    @UIField(order = 2, label = "city")
    private String city_name;

    @UIField(order = 3)
    private WeatherUnitSetting.WeatherUnit units;

    @Override
    public String getIcon() {
        return "fas fa-sun";
    }

    @Override
    public boolean isDefaultAutoScale() {
        return true;
    }

    @Override
    @SneakyThrows
    public void createWidget(JavaScriptBuilder javaScriptBuilder) {
        javaScriptBuilder.css("widget-left", "margin: 0 !important;");
        String containerId = "cow-" + System.currentTimeMillis();
        javaScriptBuilder
                .jsonParam("id", "15")
                .jsonParam("city_name", TouchHomeUtils.getIpGeoLocation().getCity())
                .jsonParam("units", entityContext.getSettingValue(WeatherUnitSetting.class))
                .jsonParam("appid", entityContext.getSettingValue(WeatherApiKeySetting.class));
        javaScriptBuilder.readyOnClient().window(window ->
                window.parameter("myWidgetParam", new JSONArray()).put(new JSONObject()
                        .put("id", "${id}")
                        .put("containerid", containerId)
                        .put("city_name", "${city_name}")
                        .put("units", "${units}")
                        .put("appid", "${appid}")))
                .addGlobalScript("//openweathermap.org/themes/openweathermap/assets/vendor/owm/js/weather-widget-generator.js");

        javaScriptBuilder.jsContent().div(style -> style.id(containerId), div -> {
        });
    }
}
