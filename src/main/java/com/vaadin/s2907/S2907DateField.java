package com.vaadin.s2907;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.data.Result;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.s2907.client.S2907DateFieldConnector.S2907DateFieldServerRpc;
import com.vaadin.shared.ui.datefield.DateResolution;
import com.vaadin.ui.DateField;

public class S2907DateField extends DateField {

    private S2907DateFieldServerRpc rpc = new S2907DateFieldServerRpc() {

        @Override
        public void update(String newDateString,
                Map<String, Integer> resolutions) {
            try {
                Class<?> dfClass = S2907DateField.this.getClass()
                        .getSuperclass().getSuperclass().getSuperclass();
                Method method = dfClass.getDeclaredMethod("getResolutions");
                method.setAccessible(true);
                @SuppressWarnings("unchecked")
                Stream<DateResolution> resolutionStream = (Stream<DateResolution>) method
                        .invoke(S2907DateField.this);

                Set<String> resolutionNames = resolutionStream.map(Enum::name)
                        .collect(Collectors.toSet());
                resolutionNames.retainAll(resolutions.keySet());
                if (!isReadOnly() && (!resolutionNames.isEmpty()
                        || newDateString != null)) {

                    // Old and new dates
                    final LocalDate oldDate = getValue();

                    LocalDate newDate;

                    boolean hasChanges = false;

                    if ("".equals(newDateString)) {

                        newDate = null;
                    } else {
                        newDate = reconstructDateFromFields(resolutions,
                                oldDate);
                    }

                    Field dfDateString = dfClass.getDeclaredField("dateString");
                    dfDateString.setAccessible(true);
                    Field dfCurrentParseErrorMessage = dfClass
                            .getDeclaredField("currentParseErrorMessage");
                    dfCurrentParseErrorMessage.setAccessible(true);

                    boolean parseErrorWasSet = dfCurrentParseErrorMessage
                            .get(S2907DateField.this) != null;
                    hasChanges |= !Objects.equals(
                            dfDateString.get(S2907DateField.this),
                            newDateString) || !Objects.equals(oldDate, newDate)
                            || parseErrorWasSet;

                    if (hasChanges) {
                        dfDateString.set(S2907DateField.this, newDateString);
                        dfCurrentParseErrorMessage.set(S2907DateField.this,
                                null);
                        if (newDateString == null || newDateString.isEmpty()) {
                            boolean valueChanged = setValue(newDate, true);
                            if (!valueChanged && parseErrorWasSet) {
                                doSetValue(newDate);
                            }
                        } else {
                            // invalid date string
                            if (resolutions.isEmpty()) {
                                Result<LocalDate> parsedDate = handleUnparsableDateString(
                                        (String) dfDateString
                                                .get(S2907DateField.this));
                                parsedDate.ifOk(v -> setValue(v, true));
                                if (parsedDate.isError()) {
                                    dfDateString.set(S2907DateField.this, null);
                                    dfCurrentParseErrorMessage.set(
                                            S2907DateField.this,
                                            parsedDate.getMessage()
                                                    .orElse("Parsing error"));

                                    if (!isDifferentValue(null)) {
                                        doSetValue(null);
                                    } else {
                                        setValue(null, true);
                                    }
                                }
                            } else {
                                setValue(newDate, true);
                            }
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void focus() {
            fireEvent(new FocusEvent(S2907DateField.this));
        }

        @Override
        public void blur() {
            fireEvent(new BlurEvent(S2907DateField.this));
        }
    };

    public S2907DateField() {
        registerRpc(rpc);
    }
}
