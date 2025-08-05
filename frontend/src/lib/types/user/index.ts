// 유저 관련 dto

export interface UserDto {
    id: number;
    username: string;
    userLoginId: string;
    role: 'ADMIN' | 'MEMBER';
    createdAt: string;
    modifiedAt: string;
}

export interface userWithUsernameDto {
    id: number;
    username: string;
    userLoginId: string;
    role: 'ADMIN' | 'MEMBER';
    createdAt: string;
    modifiedAt: string;
}

export interface userRegisterDto {
    userLoginId: string;
    username: string;
    password: string;
    confirmPassword: string;
}

export interface userLoginReqBody {
    userLoginId: string;
    password: string;
}

export interface userLoginResBody {
    item: UserDto;
    apiKey: string;
    accessToken: string;
}
