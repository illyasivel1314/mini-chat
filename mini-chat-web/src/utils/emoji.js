// Emoji categories and data for the picker
export const emojiCategories = [
  {
    name: '表情',
    emojis: ['😀','😃','😄','😁','😆','😅','🤣','😂','🙂','😉','😊','😇','🥰','😍','🤩','😘','😗','😚','😋','😛','😜','🤪','😝','🤑','🤗','🤭','🫢','🫣','🤫','🤔','🫡','🤐','🤨','😐','😑','😶','🫥','😏','😒','🙄','😬','🤥','😌','😔','😪','🤤','😴','😷','🤒','🤕','🤢','🤮','🥴','😵','🤯','🥵','🥶','😎','🤓','🧐','😕','😟','🙁','😮','😯','😲','😳','🥺','😦','😧','😨','😰','😥','😢','😭','😱','😖','😣','😞','😓','😩','😫','🥱','😤','😡','😠','🤬']
  },
  {
    name: '手势',
    emojis: ['👋','🤚','🖐','✋','🖖','🫱','🫲','🫳','🫴','👌','🤌','🤏','✌️','🤞','🫰','🤟','🤘','🤙','👈','👉','👆','🖕','👇','☝️','🫵','👍','👎','✊','👊','🤛','🤜','👏','🙌','🫶','👐','🤲','🤝','🙏','✍️','💅','🤳','💪','🦵','🦶','👂','🦻','👃','🧠','🫀','🫁']
  },
  {
    name: '爱心',
    emojis: ['❤️','🧡','💛','💚','💙','💜','🖤','🤍','🤎','💔','❣️','💕','💞','💓','💗','💖','💘','💝','💟','💌','💋','🫂','👥','👤','🗣','👣']
  },
  {
    name: '动物',
    emojis: ['🐵','🐒','🦍','🦧','🐶','🐕','🦮','🐩','🐺','🦊','🦝','🐱','🐈','🦁','🐯','🐅','🐆','🐴','🐎','🦄','🫎','🫏','🦌','🐮','🐂','🐃','🐄','🐷','🐖','🐗','🐽','🐏','🐑','🐐','🐪','🐫','🦙','🦒','🐘','🦣','🐭','🐁','🐀','🐹','🐰','🐇','🐿','🦫','🦔','🦇','🐻','🐨','🐼','🐾','🦃','🐔','🐓','🐣','🐤','🐥','🐦','🐧','🕊','🦅','🦆','🦢','🦉','🦤','🪶','🦩','🦚','🦜','🐸','🐊','🐢','🦎','🐍','🐲','🐉','🦕','🦖','🐳','🐋','🐬','🦭','🐟','🐠','🐡','🦈','🐙','🐚','🪸','🪼','🐌','🦋','🐛','🐜','🐝','🪲','🐞','🦗','🪳','🕷','🦂','🦟','🪰','🪱','🦠']
  },
  {
    name: '食物',
    emojis: ['🍏','🍎','🍐','🍊','🍋','🍌','🍉','🍇','🍓','🫐','🍈','🍒','🍑','🥭','🍍','🥥','🥝','🍅','🍆','🥑','🥦','🥬','🥒','🌶','🫑','🌽','🥕','🫒','🧄','🧅','🥔','🍠','🫘','🥐','🍞','🥖','🥨','🧀','🥚','🍳','🧈','🥞','🧇','🥓','🥩','🍗','🍖','🦴','🌭','🍔','🍟','🍕','🫓','🥪','🥙','🧆','🌮','🌯','🫔','🥗','🥘','🫕','🥫','🍝','🍜','🍲','🍛','🍣','🍱','🥟','🦪','🍤','🍚','🍘','🍥','🥠','🥮','🍢','🍡','🍧','🍨','🍦','🥧','🧁','🍰','🎂','🍮','🍭','🍬','🍫','🍿','🍩','🍪','🌰','🥜','🍯','🥛','🍼','🫖','☕️','🍵','🧃','🥤','🧋','🍶','🍺','🍻','🥂','🍷','🫗','🥃','🍸','🍹','🧉','🍾','🧊','🥄','🍴','🍽']
  }
]

// Flat list for quick lookup
export const allEmojis = emojiCategories.flatMap(c => c.emojis)

// Common GIF URLs (placeholder - in production you'd use a GIF API)
export const sampleGifs = [
  'https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExMDZzZzV1eGZ2b2RwZnFzNHF1a3p2bHF6M3YzMnBhMnNwMXNtaCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/3o7TKSjRrfIPJeiAGM/giphy.gif',
  'https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExMDZzZzV1eGZ2b2RwZnFzNHF1a3p2bHF6M3YzMnBhMnNwMXNtaCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/l0HlHFRbmaZtCHKXm/giphy.gif',
  'https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExMDZzZzV1eGZ2b2RwZnFzNHF1a3p2bHF6M3YzMnBhMnNwMXNtaCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/26AHONQ79FdWZhAI0/giphy.gif',
  'https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExMDZzZzV1eGZ2b2RwZnFzNHF1a3p2bHF6M3YzMnBhMnNwMXNtaCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/3o6Zt481isNVuQI1l6/giphy.gif',
  'https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExMDZzZzV1eGZ2b2RwZnFzNHF1a3p2bHF6M3YzMnBhMnNwMXNtaCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/3o7abldj0b3rxrZUxW/giphy.gif',
  'https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExMDZzZzV1eGZ2b2RwZnFzNHF1a3p2bHF6M3YzMnBhMnNwMXNtaCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/l0HlBO7eyXz6w2jXa/giphy.gif',
]

const COLORS = ['#07C160','#1485EE','#F56C6C','#E6A23C','#67C23A','#909399','#8B5CF6','#EC4899','#F97316','#14B8A6']

const avatarUri = (name, seed) => {
  const c = COLORS[seed % COLORS.length]
  const initials = name.slice(0, 1)
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="80" height="80"><rect width="80" height="80" rx="8" fill="${c}"/><text x="40" y="54" font-size="32" fill="#fff" text-anchor="middle" font-family="sans-serif">${initials}</text></svg>`
  return 'data:image/svg+xml,' + encodeURIComponent(svg)
}

export default avatarUri;

