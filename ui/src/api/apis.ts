import {
  BaseService,
  Field,
  GET,
  Path,
  POST,
  Query,
  ServiceBuilder,
} from "ts-retrofit";
import { Branch, GitToken, Group, Project, Repo } from "@/api/models";
import { AxiosResponse } from "axios";
import { RequestInterceptorFunction } from "ts-retrofit/lib/types/baseService";

export class GitService extends BaseService {
  @POST("/oauth/token")
  async auth(
    @Field("grant_type") grantType?: string,
    @Field("username") username?: string,
    @Field("password") password?: string
  ): Promise<AxiosResponse<GitToken>> {
    return {} as AxiosResponse<GitToken>;
  }

  @GET("/api/v4/groups")
  async listGroups(
    @Query("all_available") all = false,
    @Query("page") page = 1,
    @Query("per_page") size = 20
  ): Promise<AxiosResponse<Group[]>> {
    return {} as AxiosResponse<Group[]>;
  }

  @GET("/api/v4/groups/{id}/subgroups")
  async listSubGroups(
    @Path("id") id?: number,
    @Query("page") page = 1,
    @Query("per_page") size = 20
  ): Promise<AxiosResponse<Group[]>> {
    return {} as AxiosResponse<Group[]>;
  }

  @GET("/api/v4/groups/{id}/projects")
  async listGroupProjects(
    @Path("id") id?: number,
    @Query("page") page = 1,
    @Query("per_page") size = 20
  ): Promise<AxiosResponse<Project[]>> {
    return {} as AxiosResponse<Project[]>;
  }

  @GET("/api/v4/projects")
  async listProjects(
    @Query("page") page = 1,
    @Query("per_page") size = 20
  ): Promise<AxiosResponse<Project[]>> {
    return {} as AxiosResponse<Project[]>;
  }

  @GET("/api/v4/projects/{id}/repository/branches")
  async listBranches(
    @Path("id") id?: number,
    @Query("page") page = 1,
    @Query("per_page") size = 20
  ): Promise<AxiosResponse<Branch[]>> {
    return {} as AxiosResponse<Branch[]>;
  }

  @GET("/api/v4/projects/{id}/repository/tree")
  async listFiles(
    @Path("id") id?: number,
    @Query("path") path?: string,
    @Query("ref") branch?: string,
    @Query("page") page = 1,
    @Query("per_page") size = 20
  ): Promise<AxiosResponse<Repo[]>> {
    return {} as AxiosResponse<Repo[]>;
  }
}

export const Auth = {
  getToken(): string | null {
    return localStorage.getItem("accessToken");
  },
  setToken(token: GitToken | null) {
    if (token) {
      localStorage.setItem("accessToken", token.access_token || "");
      localStorage.setItem(
        "expireAt",
        `${Date.now() / 1000 + (token.expires_in || 0)}`
      );
    } else {
      localStorage.removeItem("accessToken");
    }
  },
  isLogin(): boolean {
    return !!this.getToken();
  },
  authFilePath(path: string, internal = true) {
    return internal
      ? `/file?path=${path}?access_token=${this.getToken()}`
      : `${path}?access_token=${this.getToken()}`;
  },
};

export const gitService = new ServiceBuilder()
  // .setEndpoint('http://localhost:8080')
  .setRequestInterceptors(((value) => {
    console.log(`Auth.getToken()`, Auth.getToken());
    value.headers.Authorization = `Bearer ${Auth.getToken()}`;
    return value;
  }) as RequestInterceptorFunction)
  .build(GitService);
