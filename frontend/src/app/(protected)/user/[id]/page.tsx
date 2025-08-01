import { getUser } from '@/lib/api/user'
import type { User } from '@/types/user'

type Props = {
    params: { id: string }   // id는 문자열 하나
}

export default async function UserPage({ params }: Props) {
    const user: User = await getUser(params.id)

    return (
        <div>
            <h1>{user.name}님의 정보</h1>
            <p>아이디: {user.userLoginId}</p>
        </div>
    )
}
