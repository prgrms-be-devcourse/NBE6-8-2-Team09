import { getUser } from '@/lib/api/user'
import type { User } from '@/types/user'
import type { PageProps } from 'next'

type Params = { id: string }

export default async function UserPage({ params }: PageProps<Params>) {
    const { id } = await params        // params가 Promise<Params>이므로 await
    const user: User = await getUser(id)

    return (
        <div>
            <h1>{user.name}님의 정보</h1>
            <p>아이디: {user.userLoginId}</p>
        </div>
    )
}
