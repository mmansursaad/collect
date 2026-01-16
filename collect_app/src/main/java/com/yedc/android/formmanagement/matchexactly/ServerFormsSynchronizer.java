package com.yedc.android.formmanagement.matchexactly;

import com.yedc.android.formmanagement.LocalFormUseCases;
import com.yedc.android.formmanagement.download.FormDownloadException;
import com.yedc.android.formmanagement.download.FormDownloader;
import com.yedc.android.formmanagement.ServerFormDetails;
import com.yedc.android.formmanagement.ServerFormsDetailsFetcher;
import com.yedc.forms.Form;
import com.yedc.forms.FormSourceException;
import com.yedc.forms.FormsRepository;
import com.yedc.forms.instances.InstancesRepository;

import java.util.List;

public class ServerFormsSynchronizer {

    private final FormsRepository formsRepository;
    private final InstancesRepository instancesRepository;
    private final FormDownloader formDownloader;
    private final ServerFormsDetailsFetcher serverFormsDetailsFetcher;

    public ServerFormsSynchronizer(ServerFormsDetailsFetcher serverFormsDetailsFetcher, FormsRepository formsRepository, InstancesRepository instancesRepository, FormDownloader formDownloader) {
        this.serverFormsDetailsFetcher = serverFormsDetailsFetcher;
        this.formsRepository = formsRepository;
        this.instancesRepository = instancesRepository;
        this.formDownloader = formDownloader;
    }

    public void synchronize() throws FormSourceException {
        List<ServerFormDetails> formList = serverFormsDetailsFetcher.fetchFormDetails();
        List<Form> formsOnDevice = formsRepository.getAll();
        formsOnDevice.stream().forEach(form -> {
            if (formList.stream().noneMatch(f -> form.getFormId().equals(f.getFormId()))) {
                LocalFormUseCases.deleteForm(formsRepository, instancesRepository, form.getDbId());
            }
        });

        boolean downloadException = false;

        for (ServerFormDetails form : formList) {
            if (form.isNotOnDevice() || form.isUpdated()) {
                try {
                    formDownloader.downloadForm(form, null, null);
                } catch (FormDownloadException.DownloadingInterrupted e) {
                    return;
                } catch (FormDownloadException e) {
                    downloadException = true;
                }
            }
        }

        if (downloadException) {
            throw new FormSourceException.FetchError();
        }
    }
}
