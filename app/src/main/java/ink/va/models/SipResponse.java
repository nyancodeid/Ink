package ink.va.models;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by PC-Comp on 4/26/2017.
 */

public class SipResponse {
    @SerializedName("success")
    @Setter
    @Getter
    private boolean success;
    @SerializedName("sip_address")
    @Setter
    @Getter
    private String sipAddress;

    @SerializedName("error")
    @Setter
    @Getter
    private String error;

    @SerializedName("email")
    @Setter
    @Getter
    private String email;
    @SerializedName("settings_url")
    @Setter
    @Getter
    private String settingsUrl;
    @SerializedName("outbound_proxy")
    @Setter
    @Getter
    private Object outboundProxy;
    @SerializedName("ldap_hostname")
    @Setter
    @Getter
    private String ldapHostname;
    @SerializedName("ldap_dn")
    @Setter
    @Getter
    private String ldapDn;
    @SerializedName("msrp_relay")
    @Setter
    @Getter
    private String msrpRelay;
    @SerializedName("xcap_root")
    @Setter
    @Getter
    private String xcapRoot;
    @SerializedName("conference_server")
    @Setter
    @Getter
    private String conferenceServer;
}
