import React, { useState } from 'react';
import { ShieldAlert, CheckCircle } from 'lucide-react';

interface TermsModalProps {
  isOpen: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export const TermsModal: React.FC<TermsModalProps> = ({ isOpen, onConfirm, onCancel }) => {
  const [is18Plus, setIs18Plus] = useState(false);
  const [acceptedRules, setAcceptedRules] = useState(false);

  if (!isOpen) return null;

  const canProceed = is18Plus && acceptedRules;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fade-in">
      <div className="glass-panel w-full max-w-md rounded-2xl p-6 sm:p-8 shadow-2xl border border-white/10 relative">
        <div className="w-12 h-12 rounded-xl bg-amber-500/20 text-amber-400 flex items-center justify-center mb-4">
          <ShieldAlert className="w-6 h-6" />
        </div>

        <h3 className="text-xl font-bold text-white mb-2">Age Verification & Safety Rules</h3>
        <p className="text-sm text-slate-300 mb-6 leading-relaxed">
          StrangerChat connects you randomly with strangers. To ensure a safe experience for everyone, please confirm before continuing.
        </p>

        <div className="space-y-4 mb-6">
          <label className="flex items-start gap-3 p-3 rounded-xl bg-slate-900/60 border border-slate-700/50 cursor-pointer hover:border-slate-600 transition-colors">
            <input
              type="checkbox"
              checked={is18Plus}
              onChange={(e) => setIs18Plus(e.target.checked)}
              className="mt-0.5 rounded border-slate-700 text-indigo-600 focus:ring-indigo-500 h-4 w-4 bg-slate-800"
            />
            <div className="text-xs sm:text-sm text-slate-200">
              <strong className="text-white block font-semibold">I am at least 18 years old</strong>
              StrangerChat is strictly restricted to adults aged 18 and older.
            </div>
          </label>

          <label className="flex items-start gap-3 p-3 rounded-xl bg-slate-900/60 border border-slate-700/50 cursor-pointer hover:border-slate-600 transition-colors">
            <input
              type="checkbox"
              checked={acceptedRules}
              onChange={(e) => setAcceptedRules(e.target.checked)}
              className="mt-0.5 rounded border-slate-700 text-indigo-600 focus:ring-indigo-500 h-4 w-4 bg-slate-800"
            />
            <div className="text-xs sm:text-sm text-slate-200">
              <strong className="text-white block font-semibold">I agree to the Community Guidelines</strong>
              No harassment, hate speech, spam, unsolicited advertising, or illegal content.
            </div>
          </label>
        </div>

        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={onCancel}
            className="flex-1 px-4 py-2.5 rounded-xl border border-slate-700 text-slate-300 font-medium text-sm hover:bg-white/5 transition-colors"
          >
            Cancel
          </button>
          <button
            type="button"
            disabled={!canProceed}
            onClick={onConfirm}
            className={`flex-1 flex items-center justify-center gap-2 px-4 py-2.5 rounded-xl font-semibold text-sm transition-all ${
              canProceed
                ? 'bg-indigo-600 hover:bg-indigo-500 text-white shadow-lg shadow-indigo-600/30 cursor-pointer'
                : 'bg-slate-800 text-slate-500 cursor-not-allowed border border-slate-700/50'
            }`}
          >
            <CheckCircle className="w-4 h-4" />
            <span>Confirm & Enter</span>
          </button>
        </div>
      </div>
    </div>
  );
};
