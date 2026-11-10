import Login from "./pages/login/login.jsx";
// TEST FOR LINKING THE FRONTEND TO THE JAVALIN BACKEND
import { useEffect, useState } from 'react'

function App() {
  const [message, setMessage] = useState('Loading...')
  const [name, setName] = useState('')
  const [text, setText] = useState('')
  const [result, setResult] = useState('')

  // TEST for sending information to the javalin back-end
  // async function handleSubmit(event) {
  //   // Not sure why it is necessary, need to learn
  //   event.preventDefault()
  //   try {
  //     const res = await fetch('/api/messages', {
  //       method: 'POST',
  //       headers: { 'Content-Type': 'application/json' },
  //       body: JSON.stringify({ name, text }),
  //     })
  //     // if (!res.ok) throw new Error('HTTP ' + res.status)
  //     const data = await res.json()
  //     setResult('Server replied: ' + data.status)
  //   }
  //   catch (err) {
  //     setResult('Error: ' + err.message)
  //     console.log(err)
  //   }
  // }
  // return (
  //   <form onSubmit={handleSubmit}>
  //     <input value={name} onChange={e => setName(e.target.value)} placeholder="Name" />
  //     <input value={text} onChange={e => setText(e.target.value)} placeholder="Message" />
  //     <button type="submit">Send</button>
  //     <p>{result}</p>
  //   </form>
  // )

  // TEST for receiving information from the Javalin back-end
  // useEffect(() => {
  //   fetch('/api/hello')            // relative URL: works in dev AND prod
  //     .then(res => res.json())
  //     .then(data => setMessage(data.message))
  //     .catch(() => {
  //       setMessage('Backend not reachable')
  //       return <Login />;
  //     })
  // }, [])
  // return <h1>{message}</h1>

  return <Login />;
}

export default App;