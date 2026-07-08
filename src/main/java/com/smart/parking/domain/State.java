package com.smart.parking.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "states")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class State {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 5)
    private String code;
}
