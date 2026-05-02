import ChannelList from '../components/messaging/ChannelList'
import MessageFeed from '../components/messaging/MessageFeed'
import MessageInput from '../components/messaging/MessageInput'

export default function MessagingPage() {
  return (
    <div className="h-full flex gap-4">
      <ChannelList />
      <div className="flex-1 flex flex-col bg-gray-900 rounded-xl overflow-hidden border border-gray-800">
        <MessageFeed />
        <MessageInput />
      </div>
    </div>
  )
}
