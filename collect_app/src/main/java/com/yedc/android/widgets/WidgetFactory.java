/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.yedc.android.widgets;

import static com.yedc.android.utilities.Appearances.MAPS;
import static com.yedc.android.utilities.Appearances.PLACEMENT_MAP;
import static com.yedc.android.utilities.Appearances.hasAppearance;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;

import androidx.lifecycle.LifecycleOwner;

import org.javarosa.core.model.Constants;
import org.javarosa.form.api.FormEntryPrompt;
import com.yedc.android.formentry.FormEntryViewModel;
import com.yedc.android.formentry.PrinterWidgetViewModel;
import com.yedc.android.formentry.questions.QuestionDetails;
import com.yedc.android.geo.MapConfiguratorProvider;
import com.yedc.android.javarosawrapper.FormController;
import com.yedc.android.listeners.AdvanceToNextListener;
import com.yedc.android.storage.StoragePathProvider;
import com.yedc.android.utilities.Appearances;
import com.yedc.android.utilities.QuestionMediaManager;
import com.yedc.android.widgets.datetime.DateTimeWidget;
import com.yedc.android.widgets.datetime.DateWidget;
import com.yedc.android.widgets.datetime.TimeWidget;
import com.yedc.android.widgets.items.LabelWidget;
import com.yedc.android.widgets.items.LikertWidget;
import com.yedc.android.widgets.items.ListMultiWidget;
import com.yedc.android.widgets.items.ListWidget;
import com.yedc.android.widgets.items.RankingWidget;
import com.yedc.android.widgets.items.SelectMultiImageMapWidget;
import com.yedc.android.widgets.items.SelectMultiMinimalWidget;
import com.yedc.android.widgets.items.SelectMultiWidget;
import com.yedc.android.widgets.items.SelectOneFromMapWidget;
import com.yedc.android.widgets.items.SelectOneImageMapWidget;
import com.yedc.android.widgets.items.SelectOneMinimalWidget;
import com.yedc.android.widgets.items.SelectOneWidget;
import com.yedc.android.widgets.range.RangeDecimalWidget;
import com.yedc.android.widgets.range.RangeIntegerWidget;
import com.yedc.android.widgets.range.RangePickerDecimalWidget;
import com.yedc.android.widgets.range.RangePickerIntegerWidget;
import com.yedc.android.widgets.utilities.ActivityGeoDataRequester;
import com.yedc.android.widgets.utilities.AudioPlayer;
import com.yedc.android.widgets.utilities.AudioRecorderRecordingStatusHandler;
import com.yedc.android.widgets.utilities.DateTimeWidgetUtils;
import com.yedc.android.widgets.utilities.FileRequester;
import com.yedc.android.widgets.utilities.GetContentAudioFileRequester;
import com.yedc.android.widgets.utilities.RecordingRequester;
import com.yedc.android.widgets.utilities.RecordingRequesterProvider;
import com.yedc.android.widgets.utilities.StringRequester;
import com.yedc.android.widgets.utilities.WaitingForDataRegistry;
import com.yedc.androidshared.system.CameraUtils;
import com.yedc.androidshared.system.IntentLauncherImpl;
import com.yedc.audiorecorder.recording.AudioRecorder;
import com.yedc.permissions.PermissionsProvider;
import com.yedc.webpage.ExternalWebPageHelper;

/**
 * Convenience class that handles creation of widgets.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class WidgetFactory {

    private static final String PICKER_APPEARANCE = "picker";

    private final Activity activity;
    private final boolean useExternalRecorder;
    private final WaitingForDataRegistry waitingForDataRegistry;
    private final QuestionMediaManager questionMediaManager;
    private final AudioPlayer audioPlayer;
    private final RecordingRequesterProvider recordingRequesterProvider;
    private final FormEntryViewModel formEntryViewModel;
    private final PrinterWidgetViewModel printerWidgetViewModel;
    private final AudioRecorder audioRecorder;
    private final LifecycleOwner viewLifecycle;
    private final FileRequester fileRequester;
    private final StringRequester stringRequester;
    private final FormController formController;
    private final AdvanceToNextListener advanceToNextListener;

    public WidgetFactory(Activity activity,
                         boolean useExternalRecorder,
                         WaitingForDataRegistry waitingForDataRegistry,
                         QuestionMediaManager questionMediaManager,
                         AudioPlayer audioPlayer,
                         RecordingRequesterProvider recordingRequesterProvider,
                         FormEntryViewModel formEntryViewModel,
                         PrinterWidgetViewModel printerWidgetViewModel,
                         AudioRecorder audioRecorder,
                         LifecycleOwner viewLifecycle,
                         FileRequester fileRequester,
                         StringRequester stringRequester,
                         FormController formController,
                         AdvanceToNextListener advanceToNextListener
    ) {
        this.activity = activity;
        this.useExternalRecorder = useExternalRecorder;
        this.waitingForDataRegistry = waitingForDataRegistry;
        this.questionMediaManager = questionMediaManager;
        this.audioPlayer = audioPlayer;
        this.recordingRequesterProvider = recordingRequesterProvider;
        this.formEntryViewModel = formEntryViewModel;
        this.printerWidgetViewModel = printerWidgetViewModel;
        this.audioRecorder = audioRecorder;
        this.viewLifecycle = viewLifecycle;
        this.fileRequester = fileRequester;
        this.stringRequester = stringRequester;
        this.formController = formController;
        this.advanceToNextListener = advanceToNextListener;
    }

    public QuestionWidget createWidgetFromPrompt(FormEntryPrompt prompt, PermissionsProvider permissionsProvider) {
        return createWidgetFromPrompt(prompt, permissionsProvider, false);
    }

    public QuestionWidget createWidgetFromPrompt(FormEntryPrompt prompt, PermissionsProvider permissionsProvider, boolean readOnlyOverride) {
        String appearance = Appearances.getSanitizedAppearanceHint(prompt);
        QuestionDetails questionDetails = new QuestionDetails(prompt, readOnlyOverride);
        QuestionWidget.Dependencies dependencies = new QuestionWidget.Dependencies(audioPlayer);

        final QuestionWidget questionWidget;
        switch (prompt.getControlType()) {
            case Constants.CONTROL_INPUT:
                switch (prompt.getDataType()) {
                    case Constants.DATATYPE_DATE_TIME:
                        questionWidget = new DateTimeWidget(activity, questionDetails, new DateTimeWidgetUtils(), waitingForDataRegistry, dependencies);
                        break;
                    case Constants.DATATYPE_DATE:
                        questionWidget = new DateWidget(activity, questionDetails, new DateTimeWidgetUtils(), waitingForDataRegistry, dependencies);
                        break;
                    case Constants.DATATYPE_TIME:
                        questionWidget = new TimeWidget(activity, questionDetails, new DateTimeWidgetUtils(), waitingForDataRegistry, dependencies);
                        break;
                    case Constants.DATATYPE_DECIMAL:
                        if (appearance.contains(Appearances.EX)) {
                            questionWidget = new ExDecimalWidget(activity, questionDetails, waitingForDataRegistry, stringRequester, dependencies);
                        } else if (appearance.equals(Appearances.BEARING)) {
                            questionWidget = new BearingWidget(activity, questionDetails, waitingForDataRegistry,
                                    (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE), dependencies);
                        } else {
                            questionWidget = new DecimalWidget(activity, questionDetails, dependencies);
                        }
                        break;
                    case Constants.DATATYPE_INTEGER:
                        if (appearance.equals(Appearances.COUNTER)) {
                            questionWidget = new CounterWidget(activity, questionDetails, dependencies);
                        } else if (appearance.contains(Appearances.EX)) {
                            questionWidget = new ExIntegerWidget(activity, questionDetails, waitingForDataRegistry, stringRequester, dependencies);
                        } else {
                            questionWidget = new IntegerWidget(activity, questionDetails, dependencies);
                        }
                        break;
                    case Constants.DATATYPE_GEOPOINT:
                        if (hasAppearance(questionDetails.getPrompt(), PLACEMENT_MAP) || hasAppearance(questionDetails.getPrompt(), MAPS)) {
                            questionWidget = new GeoPointMapWidget(activity, questionDetails, waitingForDataRegistry,
                                    new ActivityGeoDataRequester(permissionsProvider, activity), dependencies);
                        } else {
                            questionWidget = new GeoPointWidget(activity, questionDetails, waitingForDataRegistry,
                                    new ActivityGeoDataRequester(permissionsProvider, activity), dependencies);
                        }
                        break;
                    case Constants.DATATYPE_GEOSHAPE:
                        questionWidget = new GeoShapeWidget(activity, questionDetails, waitingForDataRegistry,
                                new ActivityGeoDataRequester(permissionsProvider, activity), dependencies);
                        break;
                    case Constants.DATATYPE_GEOTRACE:
                        questionWidget = new GeoTraceWidget(activity, questionDetails, waitingForDataRegistry,
                                MapConfiguratorProvider.getConfigurator(), new ActivityGeoDataRequester(permissionsProvider, activity), dependencies);
                        break;
                    case Constants.DATATYPE_BARCODE:
                        questionWidget = new BarcodeWidget(activity, questionDetails, waitingForDataRegistry, new CameraUtils(), dependencies);
                        break;
                    case Constants.DATATYPE_TEXT:
                        String query = prompt.getQuestion().getAdditionalAttribute(null, "query");
                        if (query != null) {
                            questionWidget = getSelectOneWidget(appearance, questionDetails, dependencies);
                        } else if (appearance.equals(Appearances.PRINTER)) {
                            questionWidget = new PrinterWidget(activity, questionDetails, printerWidgetViewModel, questionMediaManager, dependencies);
                        } else if (appearance.contains(Appearances.EX)) {
                            questionWidget = new ExStringWidget(activity, questionDetails, waitingForDataRegistry, stringRequester, dependencies);
                        } else if (appearance.contains(Appearances.NUMBERS)) {
                            questionWidget = new StringNumberWidget(activity, questionDetails, dependencies);
                        } else if (appearance.equals(Appearances.URL)) {
                            questionWidget = new UrlWidget(activity, questionDetails, new ExternalWebPageHelper(), dependencies);
                        } else {
                            questionWidget = new StringWidget(activity, questionDetails, dependencies);
                        }
                        break;
                    default:
                        questionWidget = new StringWidget(activity, questionDetails, dependencies);
                        break;
                }
                break;
            case Constants.CONTROL_FILE_CAPTURE:
                if (appearance.startsWith(Appearances.EX)) {
                    questionWidget = new ExArbitraryFileWidget(activity, questionDetails, questionMediaManager, waitingForDataRegistry, fileRequester, dependencies);
                } else {
                    questionWidget = new ArbitraryFileWidget(activity, questionDetails, questionMediaManager, waitingForDataRegistry, dependencies);
                }
                break;
            case Constants.CONTROL_IMAGE_CHOOSE:
                if (appearance.equals(Appearances.SIGNATURE)) {
                    questionWidget = new SignatureWidget(activity, questionDetails, questionMediaManager, waitingForDataRegistry, new StoragePathProvider().getTmpImageFilePath(), dependencies);
                } else if (appearance.contains(Appearances.ANNOTATE)) {
                    questionWidget = new AnnotateWidget(activity, questionDetails, questionMediaManager, waitingForDataRegistry, new StoragePathProvider().getTmpImageFilePath(), dependencies);
                } else if (appearance.equals(Appearances.DRAW)) {
                    questionWidget = new DrawWidget(activity, questionDetails, questionMediaManager, waitingForDataRegistry, new StoragePathProvider().getTmpImageFilePath(), dependencies);
                } else if (appearance.startsWith(Appearances.EX)) {
                    questionWidget = new ExImageWidget(activity, questionDetails, questionMediaManager, waitingForDataRegistry, fileRequester, dependencies);
                } else {
                    questionWidget = new ImageWidget(activity, questionDetails, questionMediaManager, waitingForDataRegistry, new StoragePathProvider().getTmpImageFilePath(), dependencies);
                }
                break;
            case Constants.CONTROL_OSM_CAPTURE:
                questionWidget = new OSMWidget(activity, questionDetails, waitingForDataRegistry,
                        IntentLauncherImpl.INSTANCE, formController, dependencies);
                break;
            case Constants.CONTROL_AUDIO_CAPTURE:
                RecordingRequester recordingRequester = recordingRequesterProvider.create(prompt, useExternalRecorder);
                GetContentAudioFileRequester audioFileRequester = new GetContentAudioFileRequester(activity, IntentLauncherImpl.INSTANCE, waitingForDataRegistry);

                if (appearance.startsWith(Appearances.EX)) {
                    questionWidget = new ExAudioWidget(activity, questionDetails, questionMediaManager, audioPlayer, waitingForDataRegistry, fileRequester, dependencies);
                } else {
                    questionWidget = new AudioWidget(activity, questionDetails, questionMediaManager, audioPlayer, recordingRequester, audioFileRequester, new AudioRecorderRecordingStatusHandler(audioRecorder, formEntryViewModel, viewLifecycle), dependencies);
                }
                break;
            case Constants.CONTROL_VIDEO_CAPTURE:
                if (appearance.startsWith(Appearances.EX)) {
                    questionWidget = new ExVideoWidget(activity, questionDetails, questionMediaManager, waitingForDataRegistry, fileRequester, dependencies);
                } else {
                    questionWidget = new VideoWidget(activity, questionDetails, questionMediaManager, waitingForDataRegistry, dependencies);
                }
                break;
            case Constants.CONTROL_SELECT_ONE:
                questionWidget = getSelectOneWidget(appearance, questionDetails, dependencies);
                break;
            case Constants.CONTROL_SELECT_MULTI:
                // search() appearance/function (not part of XForms spec) added by SurveyCTO gets
                // considered in each widget by calls to ExternalDataUtil.getSearchXPathExpression.
                if (appearance.contains(Appearances.MINIMAL)) {
                    questionWidget = new SelectMultiMinimalWidget(activity, questionDetails, waitingForDataRegistry, formEntryViewModel, dependencies);
                } else if (appearance.contains(Appearances.LIST_NO_LABEL)) {
                    questionWidget = new ListMultiWidget(activity, questionDetails, false, formEntryViewModel, dependencies);
                } else if (appearance.contains(Appearances.LIST)) {
                    questionWidget = new ListMultiWidget(activity, questionDetails, true, formEntryViewModel, dependencies);
                } else if (appearance.contains(Appearances.LABEL)) {
                    questionWidget = new LabelWidget(activity, questionDetails, formEntryViewModel, dependencies);
                } else if (appearance.contains(Appearances.IMAGE_MAP)) {
                    questionWidget = new SelectMultiImageMapWidget(activity, questionDetails, formEntryViewModel, dependencies);
                } else {
                    questionWidget = new SelectMultiWidget(activity, questionDetails, formEntryViewModel, dependencies);
                }
                break;
            case Constants.CONTROL_RANK:
                questionWidget = new RankingWidget(activity, questionDetails, waitingForDataRegistry, formEntryViewModel, dependencies);
                break;
            case Constants.CONTROL_TRIGGER:
                questionWidget = new TriggerWidget(activity, questionDetails, dependencies);
                break;
            case Constants.CONTROL_RANGE:
                if (appearance.startsWith(Appearances.RATING)) {
                    questionWidget = new RatingWidget(activity, questionDetails, dependencies);
                } else {
                    switch (prompt.getDataType()) {
                        case Constants.DATATYPE_INTEGER:
                            if (prompt.getAppearanceHint() != null && prompt.getAppearanceHint().contains(PICKER_APPEARANCE)) {
                                questionWidget = new RangePickerIntegerWidget(activity, questionDetails, dependencies);
                            } else {
                                questionWidget = new RangeIntegerWidget(activity, questionDetails, dependencies);
                            }
                            break;
                        case Constants.DATATYPE_DECIMAL:
                            if (prompt.getAppearanceHint() != null && prompt.getAppearanceHint().contains(PICKER_APPEARANCE)) {
                                questionWidget = new RangePickerDecimalWidget(activity, questionDetails, dependencies);
                            } else {
                                questionWidget = new RangeDecimalWidget(activity, questionDetails, dependencies);
                            }
                            break;
                        default:
                            questionWidget = new StringWidget(activity, questionDetails, dependencies);
                            break;
                    }
                }
                break;
            default:
                questionWidget = new StringWidget(activity, questionDetails, dependencies);
                break;
        }

        return questionWidget;
    }

    private QuestionWidget getSelectOneWidget(String appearance, QuestionDetails questionDetails, QuestionWidget.Dependencies dependencies) {
        final QuestionWidget questionWidget;
        boolean isQuick = appearance.contains(Appearances.QUICK);
        // search() appearance/function (not part of XForms spec) added by SurveyCTO gets
        // considered in each widget by calls to ExternalDataUtil.getSearchXPathExpression.
        if (appearance.contains(Appearances.MINIMAL)) {
            questionWidget = new SelectOneMinimalWidget(activity, questionDetails, isQuick, waitingForDataRegistry, formEntryViewModel, dependencies);
        } else if (appearance.contains(Appearances.LIKERT)) {
            questionWidget = new LikertWidget(activity, questionDetails, formEntryViewModel, dependencies);
        } else if (appearance.contains(Appearances.LIST_NO_LABEL)) {
            questionWidget = new ListWidget(activity, questionDetails, false, isQuick, formEntryViewModel, dependencies);
        } else if (appearance.contains(Appearances.LIST)) {
            questionWidget = new ListWidget(activity, questionDetails, true, isQuick, formEntryViewModel, dependencies);
        } else if (appearance.contains(Appearances.LABEL)) {
            questionWidget = new LabelWidget(activity, questionDetails, formEntryViewModel, dependencies);
        } else if (appearance.contains(Appearances.IMAGE_MAP)) {
            questionWidget = new SelectOneImageMapWidget(activity, questionDetails, isQuick, formEntryViewModel, dependencies);
        } else if (appearance.contains(Appearances.MAP)) {
            questionWidget = new SelectOneFromMapWidget(activity, questionDetails, isQuick, advanceToNextListener, dependencies);
        } else {
            questionWidget = new SelectOneWidget(activity, questionDetails, isQuick, formController, formEntryViewModel, dependencies);
        }
        return questionWidget;
    }

}
