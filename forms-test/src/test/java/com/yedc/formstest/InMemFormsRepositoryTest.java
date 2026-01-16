package com.yedc.formstest;

import org.junit.Before;

import com.yedc.forms.FormsRepository;
import com.yedc.shared.TempFiles;

import java.util.function.Supplier;

public class InMemFormsRepositoryTest extends FormsRepositoryTest {

    private String tempDirectory;

    @Before
    public void setup() {
        tempDirectory = TempFiles.createTempDir().getAbsolutePath();
    }

    @Override
    public FormsRepository buildSubject() {
        return new InMemFormsRepository(savepointsRepository);
    }

    @Override
    public FormsRepository buildSubject(Supplier<Long> clock) {
        return new InMemFormsRepository(clock, savepointsRepository);
    }

    @Override
    public String getFormFilesPath() {
        return tempDirectory;
    }
}
