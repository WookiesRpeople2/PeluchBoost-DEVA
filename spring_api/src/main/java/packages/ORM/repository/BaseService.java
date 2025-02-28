package packages.ORM.repository;

public abstract class BaseService {
    protected BaseService() {
        RepositoryServiceHandler.injectRepositories(this);
    }
}