package model;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Table;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(keyspace = "example", name = "car")
public class CarTable {
    @Column(name = "name")
    private String name;

    @Column(name = "horsepower")
    private Long horsepower;

    @Column(name = "cylinders")
    private Long cylinders;
}
