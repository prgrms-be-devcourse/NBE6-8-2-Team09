import { getUser } from '@/lib/api/user';
import type { User } from '@/types/user';

interface UserPageProps {
  params: { id: string };
}

export default async function UserPage({ params }: UserPageProps) {
  const user: User = await getUser(params.id);

  return (
    <div>
      <h1>{user.name}님의 정보</h1>
      <p>아이디: {user.userLoginId}</p>
      {/* 필요에 따라 추가 정보 표시 */}
    </div>
  );
} 