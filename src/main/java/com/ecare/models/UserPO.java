package com.ecare.models;

import com.ecare.models.base.AbstractNamedPO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.*;
import java.sql.Date;

/**
 * SubscriberPO describes a client of eCare system and contains all information
 * related to them. Also class contains information regarding list of contracts
 * signed with company.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
public class UserPO extends AbstractNamedPO {

    /**
     * The user role which describes authorities and rights of user
     */
    @Getter
    public enum Authority {
        ROLE_USER("User"),
        ROLE_DICTATOR("Dictator"),
        ROLE_ADMIN("Admin");

        private String humanReadableValue;
        private Authority(String value) {
            this.humanReadableValue = value;
        }

        public static final Authority defaultAuthority = Authority.ROLE_USER;
        public static final Map<Authority, String> stringMap;
        static {
            stringMap = new HashMap<>();
            for(final Authority en : Authority.values()) {
                stringMap.put(en, en.getHumanReadableValue());
            }
        }
    }

    public UserPO() {
        this.authority = Authority.ROLE_USER;
        this.date = new Date(Calendar.getInstance().getTime().getTime());
    }

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "birth_date")
    private Date date;

    @Column(name = "passport")
    private String passport;

    @Column(name = "address")
    private String address;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String passwordHash;

    @Column(name = "blocked")
    private boolean blocked;

    @Column(name = "authority")
    @Enumerated(EnumType.STRING)
    private Authority authority;

    @OneToOne(fetch=FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private ContractPO contract;

    @Column(name = "disabled_by")
    private Long disabledBy;

    @OneToMany(mappedBy="user", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<LocationPO> locations = new ArrayList<>();
}
