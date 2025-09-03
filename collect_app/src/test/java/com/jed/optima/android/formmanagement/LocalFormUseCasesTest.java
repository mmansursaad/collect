package com.jed.optima.android.formmanagement;

import org.junit.Test;
import com.jed.optima.forms.Form;
import com.jed.optima.forms.instances.Instance;
import com.jed.optima.formstest.FormUtils;
import com.jed.optima.formstest.InMemFormsRepository;
import com.jed.optima.formstest.InMemInstancesRepository;
import com.jed.optima.shared.TempFiles;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static com.jed.optima.formstest.InstanceUtils.buildInstance;

public class LocalFormUseCasesTest {

    private final InMemFormsRepository formsRepository = new InMemFormsRepository();
    private final InMemInstancesRepository instancesRepository = new InMemInstancesRepository();

    @Test
    public void whenFormHasDeletedInstances_deletesForm() {
        Form formToDelete = formsRepository.save(new Form.Builder()
                .formId("id")
                .version("version")
                .formFilePath(FormUtils.createXFormFile("id", "version").getAbsolutePath())
                .build());

        instancesRepository.save(new Instance.Builder()
                .formId("id")
                .formVersion("version")
                .deletedDate(0L)
                .build());

        LocalFormUseCases.deleteForm(formsRepository, instancesRepository, formToDelete.getDbId());
        assertThat(formsRepository.getAll().size(), is(0));
    }

    @Test
    public void whenOtherVersionOfFormHasInstances_deletesForm() {
        formsRepository.save(new Form.Builder()
                .formId("1")
                .version("old")
                .formFilePath(FormUtils.createXFormFile("1", "old").getAbsolutePath())
                .build());

        Form formToDelete = formsRepository.save(new Form.Builder()
                .formId("1")
                .version("new")
                .formFilePath(FormUtils.createXFormFile("1", "new").getAbsolutePath())
                .build());

        instancesRepository.save(new Instance.Builder()
                .formId("1")
                .formVersion("old")
                .build());

        LocalFormUseCases.deleteForm(formsRepository, instancesRepository, formToDelete.getDbId());
        List<Form> forms = formsRepository.getAll();
        assertThat(forms.size(), is(1));
        assertThat(forms.get(0).getVersion(), is("old"));
    }

    @Test
    public void whenFormHasNullVersion_butAnotherVersionHasInstances_deletesForm() {
        formsRepository.save(new Form.Builder()
                .formId("1")
                .version("version")
                .formFilePath(FormUtils.createXFormFile("1", "version").getAbsolutePath())
                .build());

        Form formToDelete = formsRepository.save(new Form.Builder()
                .formId("1")
                .version(null)
                .formFilePath(FormUtils.createXFormFile("1", null).getAbsolutePath())
                .build());

        instancesRepository.save(new Instance.Builder()
                .formId("1")
                .formVersion("version")
                .build());

        LocalFormUseCases.deleteForm(formsRepository, instancesRepository, formToDelete.getDbId());
        List<Form> forms = formsRepository.getAll();
        assertThat(forms.size(), is(1));
        assertThat(forms.get(0).getVersion(), is("version"));
    }

    @Test
    public void whenFormHasNullVersion_andInstancesWithNullVersion_softDeletesForm() {
        Form formToDelete = formsRepository.save(new Form.Builder()
                .formId("1")
                .version(null)
                .formFilePath(FormUtils.createXFormFile("1", null).getAbsolutePath())
                .build());

        instancesRepository.save(buildInstance("1", null, TempFiles.createTempDir().getAbsolutePath()).build());

        LocalFormUseCases.deleteForm(formsRepository, instancesRepository, formToDelete.getDbId());
        List<Form> forms = formsRepository.getAll();
        assertThat(forms.size(), is(1));
        assertThat(forms.get(0).isDeleted(), is(true));
    }

    @Test
    public void whenFormIdAndVersionCombinationIsNotUnique_andInstanceExists_hardDeletesForm() {
        Form formToDelete = formsRepository.save(new Form.Builder()
                .formId("id")
                .version("version")
                .formFilePath(FormUtils.createXFormFile("id", "version", "Form1").getAbsolutePath())
                .build());

        instancesRepository.save(new Instance.Builder()
                .formId("id")
                .formVersion("version")
                .build());

        formsRepository.save(new Form.Builder()
                .formId("id")
                .version("version")
                .formFilePath(FormUtils.createXFormFile("id", "version", "Form2").getAbsolutePath())
                .build());

        LocalFormUseCases.deleteForm(formsRepository, instancesRepository, formToDelete.getDbId());
        List<Form> forms = formsRepository.getAll();
        assertThat(forms.size(), is(1));
        assertThat(forms.get(0).getDbId(), is(2L));
    }
}
