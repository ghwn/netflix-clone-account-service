package me.ghwn.netflix.accountservice.entity;

import lombok.*;

import javax.persistence.*;

@EqualsAndHashCode(of = "id", callSuper = false)
@AllArgsConstructor
@Getter @Setter @NoArgsConstructor
@Entity
public class RefreshToken extends TimestampedEntity {

    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String value;
}
