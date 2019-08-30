new Vue({
    el: '#app',
    template: `
        <div class="wrapper">
            <div class="container">
                <ul>
                    <li v-for="message in messages">
                        {{ message }}
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
            try {
                this.messages.push(JSON.parse(evt.data));
            } catch (e) {
                this.messages.push(evt.data);
            }
        },
        sendMessage() {
            console.log(this.input);
        }
    },
    created() {
        this.ev = new EventSource('/sse');
        this.ev.addEventListener('imbus', this.notification.bind(this));
    }
});
