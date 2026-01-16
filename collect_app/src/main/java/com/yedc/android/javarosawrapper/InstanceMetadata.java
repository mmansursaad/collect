package com.yedc.android.javarosawrapper;

import com.yedc.android.formentry.audit.AuditConfig;
import com.yedc.android.utilities.FormNameUtils;

/**
 * OpenRosa metadata of a form instance.
 * <p>
 * Contains the values for the required metadata
 * fields and nothing else.
 *
 * @author mitchellsundt@gmail.com
 */
public class InstanceMetadata {
    public final String instanceId;
    public final String instanceName;
    public final AuditConfig auditConfig;

    public InstanceMetadata(String instanceId, String instanceName, AuditConfig auditConfig) {
        this.instanceId = instanceId;
        this.instanceName = FormNameUtils.normalizeFormName(instanceName, false);
        this.auditConfig = auditConfig;
    }
}
