const getters = {
  token: state => state.user.token,
  userId: state => state.user.userId,
  platform: state => state.app.platform
}

export default getters
