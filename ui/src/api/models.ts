export class Group {
    public id?: number;
    public name?: string;
    public path?: string;
    public description?: string;
    public visibility?: string;
    public lfs_enabled?: boolean;
    public avatar_url?: string;
    public web_url?: string;
    public request_access_enabled?: boolean;
    public full_name?: string;
    public full_path?: string;
    public parent_id?: number;
}

export interface Project {
    id?: number;
    description?: string;
    name?: string;
    name_with_namespace?: string;
    path?: string;
    path_with_namespace?: string;
    created_at?: string;
    default_branch?: string;
    ssh_url_to_repo?: string;
    http_url_to_repo?: string;
    web_url?: string;
    avatar_url?: string;
    star_count?: number;
    forks_count?: number;
    last_activity_at?: string;
    _links?: Links;
    archived?: boolean;
    visibility?: string;
    resolve_outdated_diff_discussions?: boolean;
    container_registry_enabled?: boolean;
    issues_enabled?: boolean;
    merge_requests_enabled?: boolean;
    wiki_enabled?: boolean;
    jobs_enabled?: boolean;
    snippets_enabled?: boolean;
    shared_runners_enabled?: boolean;
    lfs_enabled?: boolean;
    creator_id?: number;
    namespace?: Namespace;
    import_status?: string;
    open_issues_count?: number;
    public_jobs?: boolean;
    ci_config_path?: string;
    only_allow_merge_if_pipeline_succeeds?: boolean;
    request_access_enabled?: boolean;
    only_allow_merge_if_all_discussions_are_resolved?: boolean;
    printing_merge_request_link_enabled?: boolean;
    merge_method?: string;
    permissions?: Permissions;
}

export interface Links {
    self?: string;
    issues?: string;
    merge_requests?: string;
    repo_branches?: string;
    labels?: string;
    events?: string;
    members?: string;
}

export interface Namespace {
    id?: number;
    name?: string;
    path?: string;
    kind?: string;
    full_path?: string;
    parent_id?: number;
}

export interface Permissions {
    project_access?: string;
    group_access?: string;
}

export class Repo {
    id?: string;
    name?: string;
    type?: string;
    path?: string;
    mode?: string;
}

export class Branch {
    public name?: string;
    public merged?: boolean;
    public protected?: boolean;
    public developers_can_push?: boolean;
    public developers_can_merge?: boolean;
    public commit?: Commit;
}

export class Commit {
    public author_email?: string;
    public author_name?: string;
    public authored_date?: string;
    public committed_date?: string;
    public committer_email?: string;
    public committer_name?: string;
    public id?: string;
    public short_id?: string;
    public title?: string;
    public message?: string;
    public parent_ids?: string[];
}

export class GitToken {
    public access_token?: string;
    public expires_in?: number;
}
