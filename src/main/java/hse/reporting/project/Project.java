package hse.reporting.project;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Project state and Manager assignment are owned by Project Management. */
@Entity
@Table(name = "project")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "manager_email")
    private String managerEmail;

    @Column(nullable = false)
    private boolean closed;

    protected Project() {
    }

    public Project(String name, String managerEmail, boolean closed) {
        this.name = name;
        this.managerEmail = managerEmail;
        this.closed = closed;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getManagerEmail() {
        return managerEmail;
    }

    public boolean isClosed() {
        return closed;
    }
}
