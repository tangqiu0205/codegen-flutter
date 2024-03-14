import {Group, Project, Repo} from '@/api/models'

export enum PathSectionType {
    HOME, GROUP, PROJECT, DIR, FILE
}

export type Node = Group | Project | Repo

export interface PathSection {
    type: PathSectionType;
    parent?: number;
    node?: Node;
}

export const HomePathSection = {
    type: PathSectionType.HOME
}

export function sectionIcon(type: PathSectionType): string {
    switch (type) {
        case PathSectionType.HOME:
            return 'el-icon-house'
        case PathSectionType.GROUP:
            return 'el-icon-receiving'
        case PathSectionType.PROJECT:
            return 'el-icon-collection'
        case PathSectionType.DIR:
            return 'el-icon-folder'
        case PathSectionType.FILE:
            return 'el-icon-document'
    }
}
