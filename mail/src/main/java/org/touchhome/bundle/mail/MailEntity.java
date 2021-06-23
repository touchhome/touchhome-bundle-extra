package org.touchhome.bundle.mail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.Email;
import org.touchhome.bundle.api.Lang;
import org.touchhome.bundle.api.entity.CommunicationEntity;
import org.touchhome.bundle.api.entity.HasStatusAndMsg;
import org.touchhome.bundle.api.model.KeyValueEnum;
import org.touchhome.bundle.api.model.Status;
import org.touchhome.bundle.api.ui.UISidebarChildren;
import org.touchhome.bundle.api.ui.field.UIField;
import org.touchhome.bundle.api.ui.field.UIFieldGroup;
import org.touchhome.bundle.api.ui.field.UIFieldType;
import org.touchhome.bundle.api.ui.field.color.UIFieldColorStatusMatch;
import org.touchhome.bundle.api.util.SecureString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Min;
import java.util.function.Function;

@Getter
@Setter
@Entity
@Accessors(chain = true)
@UISidebarChildren(icon = "fas fa-envelope", color = "#CC3300")
public class MailEntity extends CommunicationEntity<MailEntity> implements HasStatusAndMsg<MailEntity> {

    public static final String PREFIX = "mail_";

    @Getter
    @UIField(order = 22, readOnly = true, hideOnEmpty = true)
    @UIFieldColorStatusMatch
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private Status status;

    @Getter
    @UIField(order = 23, readOnly = true, hideOnEmpty = true)
    @Column(length = 512)
    private String statusMessage;

    @UIField(order = 1, required = true, readOnly = true, hideOnEmpty = true, fullWidth = true, bg = "#334842", type = UIFieldType.HTML)
    public String getDescription() {
        if (StringUtils.isEmpty(getSender())
                || StringUtils.isEmpty(getSmtpHostname())
                || StringUtils.isEmpty(getSmtpUser())
                || StringUtils.isEmpty(getSmtpPassword())
                || StringUtils.isEmpty(getPop3Hostname())
                || StringUtils.isEmpty(getPop3Password())
                || StringUtils.isEmpty(getPop3User())) {
            return Lang.getServerMessage("mail.description");
        }
        return null;
    }

    @UIField(order = 10, inlineEdit = true)
    public PredefinedMailType getPredefinedMailType() {
        return getJsonDataEnum("def_type", PredefinedMailType.Gmail);
    }

    public MailEntity setPredefinedMailType(PredefinedMailType value) {
        return setJsonData("def_type", value);
    }

    @UIField(order = 30, required = true, inlineEditWhenEmpty = true)
    @UIFieldGroup("SMTP")
    public String getSender() {
        return getJsonData("sender");
    }

    public MailEntity setSender(String value) {
        return setJsonData("sender", value);
    }

    @UIField(order = 40, required = true, inlineEditWhenEmpty = true)
    @UIFieldGroup("SMTP")
    public String getSmtpHostname() {
        return getJsonData("smtp_hostname", getPredefinedMailType().smtpHostname);
    }

    public MailEntity setSmtpHostname(String value) {
        return setJsonData("smtp_hostname", value);
    }

    @UIField(order = 50)
    @UIFieldGroup("SMTP")
    public int getSmtpPort() {
        return getJsonData("smtp_port", getSmtpSecurity() == Security.SSL ? 465 : 25);
    }

    public MailEntity setSmtpPort(int value) {
        return setJsonData("smtp_port", value);
    }

    @UIField(order = 60)
    @UIFieldGroup("SMTP")
    public Security getSmtpSecurity() {
        return getJsonDataEnum("smtp_security", getPredefinedMailType().smtpSecurity);
    }

    public MailEntity setSmtpSecurity(Security value) {
        return setJsonData("smtp_security", value);
    }

    @UIField(order = 70, required = true, inlineEditWhenEmpty = true)
    @UIFieldGroup("SMTP")
    public String getSmtpUser() {
        return getJsonData("smtp_user", "");
    }

    public MailEntity setSmtpUser(String value) {
        return setJsonData("smtp_user", value);
    }

    @UIField(order = 80, required = true, inlineEditWhenEmpty = true)
    @UIFieldGroup("SMTP")
    public SecureString getSmtpPassword() {
        return new SecureString(getJsonData("smtp_password", ""));
    }

    public MailEntity setSmtpPassword(String value) {
        return setJsonData("smtp_password", value);
    }

    @UIField(order = 100)
    public FetchProtocolType getMailFetchProtocolType() {
        return getJsonDataEnum("fetch_protocol", FetchProtocolType.IMAP);
    }

    public MailEntity setMailFetchProtocolType(FetchProtocolType value) {
        return setJsonData("fetch_protocol", value);
    }

    @UIField(order = 200, required = true, inlineEditWhenEmpty = true)
    @UIFieldGroup("POP3/IMAP")
    public String getPop3User() {
        return getJsonData("pop3_user", "");
    }

    public MailEntity setPop3User(String value) {
        return setJsonData("pop3_user", value);
    }

    @UIField(order = 210, required = true, inlineEditWhenEmpty = true)
    @UIFieldGroup("POP3/IMAP")
    public SecureString getPop3Password() {
        return new SecureString(getJsonData("pop3_password", ""));
    }

    public MailEntity setPop3Password(String value) {
        return setJsonData("pop3_password", value);
    }

    @UIField(order = 220, required = true, inlineEditWhenEmpty = true)
    @UIFieldGroup("POP3/IMAP")
    public String getPop3Hostname() {
        return getJsonData("pop3_hostname", getPredefinedMailType().imapHostname);
    }

    public MailEntity setPop3Hostname(String value) {
        return setJsonData("pop3_hostname", value);
    }

    @UIField(order = 230)
    @UIFieldGroup("POP3/IMAP")
    public int getPop3Port() {
        return getJsonData("pop3_port", getMailFetchProtocolType().defaultPortFn.apply(this));
    }

    public MailEntity setPop3Port(int value) {
        return setJsonData("pop3_port", value);
    }

    @UIField(order = 240)
    @UIFieldGroup("POP3/IMAP")
    public Security getPop3Security() {
        return getJsonDataEnum("pop3_security", getPredefinedMailType().imapSecurity);
    }

    public MailEntity setPop3Security(Security value) {
        return setJsonData("pop3_security", value);
    }

    @UIField(order = 250)
    @Min(10)
    @UIFieldGroup("POP3/IMAP")
    public int getPop3RefreshTime() {
        return getJsonData("pop3_refresh_time", 60);
    }

    public MailEntity setPop3RefreshTime(int value) {
        return setJsonData("pop3_refresh_time", value);
    }

    @RequiredArgsConstructor
    enum FetchProtocolType {
        IMAP(mailEntity -> mailEntity.getPop3Security() == Security.SSL ? 993 : 143),
        POP3(mailEntity -> mailEntity.getPop3Security() == Security.SSL ? 995 : 110);

        private final Function<MailEntity, Integer> defaultPortFn;
    }

    @RequiredArgsConstructor
    enum Security implements KeyValueEnum {
        PLAIN("plain"), START_TLS("StarTTLS"), SSL("SSL/TLS");
        private final String title;

        @Override
        public String getValue() {
            return title;
        }

        public void prepareMail(Email mail, MailEntity mailEntity) {
            switch (this) {
                case SSL:
                    mail.setSSLOnConnect(true);
                    mail.setSslSmtpPort(String.valueOf(mailEntity.getSmtpPort()));
                    break;
                case START_TLS:
                    mail.setStartTLSEnabled(true);
                    mail.setStartTLSRequired(true);
                    mail.setSmtpPort(mailEntity.getSmtpPort());
                    break;
                case PLAIN:
                    mail.setSmtpPort(mailEntity.getSmtpPort());
            }
        }
    }

    @RequiredArgsConstructor
    enum PredefinedMailType {
        None("", Security.PLAIN, "", Security.SSL),
        Gmail("smtp.gmail.com", Security.SSL, "imap.gmail.com", Security.SSL);
        public final String smtpHostname;
        public final Security smtpSecurity;
        public final String imapHostname;
        public final Security imapSecurity;
    }

    @Override
    public String getDefaultName() {
        return "MailBot";
    }

    @Override
    public String getEntityPrefix() {
        return PREFIX;
    }
}
