new Vue({
  el: '#app',
  template: `
        <div class="wrapper">
            <div class="container">
                <ul>
                    <li v-for="message in messages">
                        <span v-if="message.data.body" v-html="message.data.body"/>
                    </li>
                </ul>
            </div>
            <div class="footer">
                <div class="holder01">
                    <textarea v-model.trim="input"/>
                </div>
                <div class="holder02">
                    <button @click="sendMessage">Send</button>
                </div>
            </div>
        </div>
        
    `,
  data: {
    messages: [],
    input: '',
  },
  methods: {
    notification(evt) {
      if (this.messages.length > 10) {
        this.messages.length = 0;
      }
      let message;
      try {
        message = JSON.parse(evt.data);
      } catch (e) {
        message = {};
      }
      this.messages.push(message);
    },
    sendMessage() {
      fetch(`/message/1/${this.input}`)
        .then(function() {
          console.log(`Send message successfully!`);
        });
    }
  },
  created() {
    this.ev = new EventSource('/sse');
    this.ev.addEventListener('imbus', this.notification.bind(this));
  }
});
