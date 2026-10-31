import React, { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useChatSocket } from '../services/useChatSocket';
import { ReportModal } from '../components/ReportModal';
import { api } from '../services/api';
import {
  Send,
  RotateCcw,
  Square,
  Flag,
  Ban,
  Radio,
  Clock,
  Sparkles,
  Loader2,
  AlertCircle
} from 'lucide-react';

export const ChatPage: React.FC = () => {
  const { token, isGuest, hasAgreedToTerms } = useAuth();
  const navigate = useNavigate();

  const [inputMessage, setInputMessage] = useState('');
  const [isReportOpen, setIsReportOpen] = useState(false);
  const [reportSuccess, setReportSuccess] = useState<string | null>(null);
  const [blockedNotice, setBlockedNotice] = useState<string | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  const {
    queueState,
    roomId,
    conversationId,
    messages,
    isPartnerTyping,
    statusMessage,
    isConnectedToServer,
    joinQueue,
    leaveQueue,
    sendMessage,
    sendTyping,
    nextStranger,
    leaveRoom,
  } = useChatSocket(token);

  // Auto-redirect if terms not agreed
  useEffect(() => {
    if (!hasAgreedToTerms) {
      navigate('/');
    }
  }, [hasAgreedToTerms, navigate]);

  // Auto-join queue when socket connects and state is IDLE
  useEffect(() => {
    if (isConnectedToServer && queueState === 'IDLE') {
      joinQueue();
    }
  }, [isConnectedToServer, queueState, joinQueue]);

  // Auto-scroll to latest message
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, isPartnerTyping]);

  // Focus input on match
  useEffect(() => {
    if (queueState === 'CONNECTED') {
      inputRef.current?.focus();
    }
  }, [queueState]);

  // ESC shortcut for Next
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        if (queueState === 'CONNECTED' || queueState === 'PARTNER_LEFT') {
          handleNext();
        }
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [queueState]);

  const handleNext = () => {
    setReportSuccess(null);
    setBlockedNotice(null);
    setInputMessage('');
    nextStranger();
  };

  const handleStop = () => {
    leaveRoom();
  };

  const handleSendMessage = (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!inputMessage.trim() || queueState !== 'CONNECTED') return;

    sendMessage(inputMessage);
    sendTyping(false);
    setInputMessage('');
    inputRef.current?.focus();
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setInputMessage(e.target.value);
    if (queueState === 'CONNECTED') {
      sendTyping(e.target.value.length > 0);
    }
  };

  const handleBlockPartner = async () => {
    if (!roomId) return;
    try {
      await api.blockUser({ roomId, conversationId: conversationId || undefined });
      setBlockedNotice('Stranger blocked. You will not be paired with them again.');
      setTimeout(() => {
        handleNext();
      }, 1200);
    } catch (err: any) {
      console.error('Failed to block partner', err);
    }
  };

  return (
    <div className="flex flex-col h-[calc(100vh-4rem)] max-w-5xl mx-auto px-2 sm:px-4 py-3 sm:py-4">
      {/* Top Session Status Bar */}
      <div className="glass-panel rounded-2xl px-4 py-3 mb-3 flex flex-wrap items-center justify-between gap-3 shadow-lg border border-white/10">
        <div className="flex items-center gap-3">
          {queueState === 'WAITING' ? (
            <div className="flex items-center gap-2 text-indigo-400">
              <Loader2 className="w-5 h-5 animate-spin" />
              <span className="text-sm font-semibold tracking-wide animate-soft-pulse">
                {statusMessage}
              </span>
            </div>
          ) : queueState === 'CONNECTED' ? (
            <div className="flex items-center gap-2">
              <span className="relative flex h-3 w-3">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
                <span className="relative inline-flex rounded-full h-3 w-3 bg-emerald-500"></span>
              </span>
              <span className="text-sm font-bold text-white">Chatting with Stranger</span>
              {!isGuest && (
                <span className="text-xs px-2 py-0.5 rounded-full bg-indigo-500/10 text-indigo-300 border border-indigo-500/20">
                  Saving to history
                </span>
              )}
            </div>
          ) : queueState === 'PARTNER_LEFT' ? (
            <div className="flex items-center gap-2 text-amber-400">
              <AlertCircle className="w-4 h-4" />
              <span className="text-sm font-semibold">Stranger has left the conversation</span>
            </div>
          ) : (
            <div className="flex items-center gap-2 text-slate-400">
              <Radio className="w-4 h-4" />
              <span className="text-sm font-medium">Ready</span>
            </div>
          )}
        </div>

        {/* Action Controls for Connected Session */}
        <div className="flex items-center gap-2">
          {queueState === 'CONNECTED' && (
            <>
              <button
                onClick={() => setIsReportOpen(true)}
                className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-800/80 hover:bg-rose-500/15 text-slate-300 hover:text-rose-300 text-xs font-semibold border border-slate-700 hover:border-rose-500/30 transition-colors"
                title="Report Inappropriate Behavior"
              >
                <Flag className="w-3.5 h-3.5" />
                <span className="hidden sm:inline">Report</span>
              </button>

              {!isGuest && (
                <button
                  onClick={handleBlockPartner}
                  className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-800/80 hover:bg-amber-500/15 text-slate-300 hover:text-amber-300 text-xs font-semibold border border-slate-700 hover:border-amber-500/30 transition-colors"
                  title="Block this stranger from rematching"
                >
                  <Ban className="w-3.5 h-3.5" />
                  <span className="hidden sm:inline">Block</span>
                </button>
              )}

              <button
                onClick={handleStop}
                className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-rose-950/40 hover:bg-rose-900/60 text-rose-300 text-xs font-semibold border border-rose-800/40 transition-colors"
                title="Stop this chat"
              >
                <Square className="w-3.5 h-3.5 fill-current" />
                <span>Stop</span>
              </button>
            </>
          )}

          {queueState === 'WAITING' && (
            <button
              onClick={leaveQueue}
              className="px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-medium border border-slate-700 transition-colors"
            >
              Cancel
            </button>
          )}
        </div>
      </div>

      {/* Moderation / Block notice toast */}
      {(reportSuccess || blockedNotice) && (
        <div className="mb-2 p-2.5 rounded-xl bg-emerald-500/15 border border-emerald-500/30 text-emerald-300 text-xs flex items-center justify-between">
          <span>{reportSuccess || blockedNotice}</span>
          <button
            onClick={() => {
              setReportSuccess(null);
              setBlockedNotice(null);
            }}
            className="text-emerald-400 hover:text-white"
          >
            ✕
          </button>
        </div>
      )}

      {/* Middle Messages Area */}
      <div className="flex-1 glass-card rounded-2xl p-4 overflow-y-auto flex flex-col justify-between shadow-inner border border-white/5 relative">
        {queueState === 'WAITING' && (
          <div className="my-auto flex flex-col items-center justify-center text-center p-6">
            <div className="w-16 h-16 rounded-3xl bg-indigo-600/15 text-indigo-400 flex items-center justify-center mb-4 animate-soft-pulse border border-indigo-500/20">
              <Sparkles className="w-8 h-8" />
            </div>
            <h3 className="text-xl font-bold text-white mb-2">Looking for a Stranger...</h3>
            <p className="text-sm text-slate-400 max-w-sm">
              Please wait a moment while we match you with another online user.
            </p>
          </div>
        )}

        {queueState !== 'WAITING' && messages.length === 0 && (
          <div className="my-auto text-center text-slate-500 text-sm">
            Say something to break the ice!
          </div>
        )}

        {/* Message Thread */}
        <div className="space-y-3.5">
          {messages.map((msg) => {
            if (msg.isSystem) {
              return (
                <div key={msg.id} className="flex justify-center my-4">
                  <div className="px-4 py-1.5 rounded-full bg-slate-900/80 border border-slate-700/60 text-xs text-slate-400 font-medium tracking-wide">
                    {msg.content}
                  </div>
                </div>
              );
            }

            return (
              <div
                key={msg.id}
                className={`flex flex-col ${msg.fromMe ? 'items-end' : 'items-start'} group`}
              >
                <div className="flex items-center gap-1.5 mb-1 px-1">
                  <span
                    className={`text-[11px] font-bold ${
                      msg.fromMe ? 'text-indigo-400' : 'text-cyan-400'
                    }`}
                  >
                    {msg.senderLabel}
                  </span>
                  <span className="text-[10px] text-slate-500 flex items-center gap-0.5">
                    <Clock className="w-2.5 h-2.5" />
                    {new Date(msg.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </span>
                </div>

                <div
                  className={`max-w-[85%] sm:max-w-[70%] px-4 py-2.5 rounded-2xl text-sm leading-relaxed break-words shadow-md ${
                    msg.fromMe
                      ? 'bg-gradient-to-r from-indigo-600 to-indigo-500 text-white rounded-tr-none shadow-indigo-600/20'
                      : 'bg-slate-800/90 text-slate-100 border border-slate-700/70 rounded-tl-none'
                  }`}
                  dangerouslySetInnerHTML={{ __html: msg.content }}
                />
              </div>
            );
          })}

          {/* Typing Indicator Bubble */}
          {isPartnerTyping && (
            <div className="flex flex-col items-start animate-fade-in">
              <span className="text-[11px] font-bold text-cyan-400 mb-1 px-1">Stranger</span>
              <div className="px-4 py-2.5 rounded-2xl rounded-tl-none bg-slate-800/80 border border-slate-700/60 flex items-center gap-1.5 shadow-sm">
                <span className="w-2 h-2 rounded-full bg-cyan-400 animate-bounce" style={{ animationDelay: '0ms' }}></span>
                <span className="w-2 h-2 rounded-full bg-cyan-400 animate-bounce" style={{ animationDelay: '150ms' }}></span>
                <span className="w-2 h-2 rounded-full bg-cyan-400 animate-bounce" style={{ animationDelay: '300ms' }}></span>
                <span className="text-xs text-slate-400 ml-1.5 font-medium">typing...</span>
              </div>
            </div>
          )}

          <div ref={messagesEndRef} />
        </div>
      </div>

      {/* Disconnected Stranger Action Banner */}
      {queueState === 'PARTNER_LEFT' && (
        <div className="mt-3 p-4 rounded-2xl bg-slate-900/90 border border-slate-800 flex flex-col sm:flex-row items-center justify-between gap-3 shadow-xl">
          <div className="text-sm text-slate-300 font-medium">
            Your conversation has ended. Ready for the next one?
          </div>
          <button
            onClick={handleNext}
            className="w-full sm:w-auto flex items-center justify-center gap-2 px-6 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-sm font-bold shadow-lg shadow-indigo-600/30 transition-all hover:scale-105"
          >
            <RotateCcw className="w-4 h-4" />
            <span>Find New Stranger (ESC)</span>
          </button>
        </div>
      )}

      {/* Bottom Chat Input Form */}
      <form onSubmit={handleSendMessage} className="mt-3 flex items-center gap-2 sm:gap-3">
        {/* Next Stranger Button */}
        <button
          type="button"
          onClick={handleNext}
          className="flex items-center justify-center gap-1.5 px-4 sm:px-5 py-3 rounded-2xl bg-slate-800 hover:bg-slate-700 text-slate-200 font-bold text-xs sm:text-sm border border-slate-700 transition-all hover:scale-[1.02] shrink-0"
          title="Disconnect and find new stranger (Shortcut: ESC)"
        >
          <RotateCcw className="w-4 h-4 text-indigo-400" />
          <span className="hidden md:inline">Next</span>
          <kbd className="hidden lg:inline px-1 py-0.5 text-[10px] bg-slate-900 text-slate-400 rounded border border-slate-700">ESC</kbd>
        </button>

        {/* Input box */}
        <div className="relative flex-1">
          <input
            ref={inputRef}
            type="text"
            value={inputMessage}
            onChange={handleInputChange}
            disabled={queueState !== 'CONNECTED'}
            maxLength={1000}
            placeholder={
              queueState === 'CONNECTED'
                ? 'Type a message... (Press Enter to send)'
                : queueState === 'WAITING'
                ? 'Waiting for partner to connect...'
                : 'Click Next to find a stranger'
            }
            className="w-full pl-4 pr-16 py-3 rounded-2xl bg-slate-900/90 border border-slate-700/80 text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 disabled:opacity-50 transition-all"
          />

          {inputMessage.length > 0 && (
            <span className="absolute right-3 top-1/2 -translate-y-1/2 text-[10px] text-slate-500 font-mono">
              {inputMessage.length}/1000
            </span>
          )}
        </div>

        {/* Send Button */}
        <button
          type="submit"
          disabled={queueState !== 'CONNECTED' || !inputMessage.trim()}
          className="flex items-center justify-center w-12 h-12 rounded-2xl bg-indigo-600 hover:bg-indigo-500 text-white shadow-lg shadow-indigo-600/30 disabled:opacity-40 disabled:hover:bg-indigo-600 transition-all hover:scale-105 active:scale-95 shrink-0"
          title="Send message"
        >
          <Send className="w-5 h-5 fill-current" />
        </button>
      </form>

      <ReportModal
        isOpen={isReportOpen}
        conversationId={conversationId}
        onClose={() => setIsReportOpen(false)}
        onReportSubmitted={() => {
          setReportSuccess('Thank you. Your report has been submitted to moderators.');
        }}
      />
    </div>
  );
};
