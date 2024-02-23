package org.acme;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import net.fortuna.ical4j.model.Calendar;


@Entity
@Table(name = "POJO")
public class Pojo {

    @Id
    public String name;
    @Column(columnDefinition = "TEXT")
    public Calendar calendar;

    @Override
    public String toString() {
        return "Pojo{" + "name=" + name + ", calendar=" + calendar + '}';
    }

}
