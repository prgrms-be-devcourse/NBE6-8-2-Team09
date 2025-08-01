import { getUser } from '@/lib/api/user'
import type { User } from '@/types/user'

interface UserPageProps {
    params: Promise<{ id: string }>
}

export default async function UserPage({ params }: UserPageProps) {
    const { id } = await params
    const user: User = await getUser(id)

    return (
        <div>
            <h1>{user.name}님의 정보</h1>
            <p>아이디: {user.userLoginId}</p>
        </div>
    )
}
